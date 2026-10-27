package com.chobolevel.api.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.Slice
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import com.tngtech.archunit.library.freeze.FreezingArchRule
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

// 모듈·패키지 의존 규칙(decisions/module-and-package-structure.md)을 코드로 고정한다.
// 이미 존재하는 위반은 archunit_store에 얼려 두고(FreezingArchRule), 새 위반만 막는다.
// 재편 단계에서 위반을 고치면 저장소 항목이 사라지므로, 허용 목록은 줄어드는 방향으로만 바뀐다.
class DependencyRuleTest : BehaviorSpec({

    val classes: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.chobolevel")

    // 기능 패키지 안에서 허용하는 레이어 이름(decisions/module-and-package-structure.md의 "패키지 표준").
    val allowedLayerNames: Set<String> = setOf(
        "controller", "service", "dto", "converter", "validator", "updater",
        "consumer", "scheduler", "provider", "config"
    )
    // common은 레이어가 아니라 역할로 나눈다. 새 잡동사니 패키지가 생기지 않도록 허용 역할을 고정한다.
    val allowedCommonRoles: Set<String> = setOf(
        "web",
        "security",
        "s3",
        "time",
        "transaction",
        "exception"
    )
    // 애그리거트(루트 + 소속 하위 엔티티) 구성. 외부에서는 루트 패키지로만 접근한다.
    val aggregateMembers: Map<String, List<String>> = mapOf(
        "user" to listOf("follow", "image"),
        "record" to listOf("emotion", "like", "review", "tag", "view"),
        "subject" to listOf("image"),
        "emotion" to listOf("category"),
        "notification" to listOf("dispatch")
    )
    val featureNames: List<String> = listOf("emotion", "notification", "record", "subject", "upload", "user")

    fun check(rule: ArchRule) {
        FreezingArchRule.freeze(rule).check(classes)
    }

    given("모듈 의존 방향") {
        then("domain은 api에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.chobolevel.api..")
            )
        }
    }

    given("domain의 외부 기술 의존") {
        then("domain은 web, kafka, redis, mail, http 클라이언트에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web..",
                        "org.springframework.kafka..",
                        "org.springframework.data.redis..",
                        "org.springframework.mail..",
                        "org.redisson..",
                        "com.amazonaws..",
                        "com.resend..",
                        "okhttp3.."
                    )
            )
        }

        then("domain에는 스프링 빈(@Configuration, @Component)이 없다") {
            // 빈 등록은 구현 모듈(infrastructure)과 api가 맡는다. domain은 엔티티·포트·값 객체만 둔다.
            // 엔티티 스캔을 등록하는 자동 구성 클래스(DomainConfigurationLoader)만 예외다.
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..")
                    .and().doNotHaveSimpleName("DomainConfigurationLoader")
                    .should().beAnnotatedWith(org.springframework.context.annotation.Configuration::class.java)
                    .orShould().beMetaAnnotatedWith(org.springframework.stereotype.Component::class.java)
            )
        }
    }

    given("api의 구현 기술 의존") {
        then("api는 Redis·Redisson·Resend 구현 기술에 의존하지 않는다") {
            // 이 기술들은 infrastructure가 맡는다. api는 domain의 포트(CacheProvider 등)로만 쓴다.
            check(
                noClasses().that().resideInAPackage("com.chobolevel.api..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.data.redis..",
                        "org.redisson..",
                        "com.resend.."
                    )
            )
        }
    }

    given("패키지 순환") {
        then("domain의 최상위 도메인끼리 순환 참조하지 않는다") {
            check(slices().matching("com.chobolevel.domain.(*)..").should().beFreeOfCycles())
        }

        then("api의 최상위 기능끼리 순환 참조하지 않는다") {
            // common은 횡단 관심사라 아래 "common의 역할" 규칙이 따로 막는다.
            // (순환 위반 메시지는 의존 나열 순서가 실행마다 달라 얼려 둘 수 없기도 하다.)
            check(
                slices().matching("com.chobolevel.api.(*)..")
                    .that(DescribedPredicate.describe("common이 아닌 슬라이스") { slice: Slice -> slice.description != "Slice common" })
                    .should().beFreeOfCycles()
            )
        }
    }

    given("common의 역할") {
        then("api의 common은 기능 패키지에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.api.common..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                        "com.chobolevel.api.emotion..",
                        "com.chobolevel.api.notification..",
                        "com.chobolevel.api.record..",
                        "com.chobolevel.api.subject..",
                        "com.chobolevel.api.upload..",
                        "com.chobolevel.api.user.."
                    )
            )
        }

        then("domain의 common은 도메인 패키지에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain.common..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                        "com.chobolevel.domain.emotion..",
                        "com.chobolevel.domain.notification..",
                        "com.chobolevel.domain.record..",
                        "com.chobolevel.domain.subject..",
                        "com.chobolevel.domain.user.."
                    )
            )
        }
    }

    given("기능 패키지의 레이어 이름") {
        then("기능 패키지의 클래스는 허용된 레이어 이름의 패키지에 있다") {
            val featureRoots: Set<String> = featureNames.map { "com.chobolevel.api.$it" }.toSet()
            val inAllowedLayer: ArchCondition<JavaClass> = object : ArchCondition<JavaClass>("reside in a package named after an allowed layer") {
                override fun check(item: JavaClass, events: ConditionEvents) {
                    val packageName: String = item.packageName
                    val layerName: String = packageName.substringAfterLast('.')
                    if (packageName !in featureRoots && layerName !in allowedLayerNames) {
                        events.add(SimpleConditionEvent.violated(item, "${item.name} is in package '$packageName' whose name '$layerName' is not an allowed layer name"))
                    }
                }
            }
            check(
                classes().that().resideInAnyPackage(*featureNames.map { "com.chobolevel.api.$it.." }.toTypedArray())
                    .should(inAllowedLayer)
            )
        }
    }

    given("common의 역할 구성") {
        then("common 하위 패키지는 허용된 역할 이름만 사용한다") {
            val inAllowedRole: ArchCondition<JavaClass> = object : ArchCondition<JavaClass>("reside in a common package named after an allowed role") {
                override fun check(item: JavaClass, events: ConditionEvents) {
                    val relative: String = item.packageName.removePrefix("com.chobolevel.api.common").removePrefix(".")
                    val role: String = relative.substringBefore('.')
                    if (role !in allowedCommonRoles) {
                        events.add(SimpleConditionEvent.violated(item, "${item.name} is in package '${item.packageName}' whose role '$role' is not an allowed common role"))
                    }
                }
            }
            check(classes().that().resideInAPackage("com.chobolevel.api.common..").should(inAllowedRole))
        }

        then("common의 역할 패키지끼리 순환 참조하지 않는다") {
            check(slices().matching("com.chobolevel.api.common.(*)..").should().beFreeOfCycles())
        }
    }

    given("domain의 애그리거트 경계") {
        aggregateMembers.forEach { (root, members) ->
            then("$root 애그리거트의 하위 패키지(${members.joinToString()})는 애그리거트 밖의 domain 코드가 직접 참조하지 않는다") {
                check(
                    noClasses().that().resideInAPackage("com.chobolevel.domain..")
                        .and().resideOutsideOfPackage("com.chobolevel.domain.$root..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                            *members.map { "com.chobolevel.domain.$root.$it.." }.toTypedArray()
                        )
                )
            }
        }
    }

    given("domain의 레이어 방향") {
        then("entity는 repository에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..entity..")
                    .should().dependOnClassesThat().resideInAPackage("com.chobolevel.domain..repository..")
            )
        }

        then("vo는 entity와 repository에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..vo..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                        "com.chobolevel.domain..entity..",
                        "com.chobolevel.domain..repository.."
                    )
            )
        }
    }

    given("infrastructure 모듈의 경계") {
        // Gradle 의존(domain ← infrastructure, api는 runtimeOnly)이 이미 컴파일 시점에 막지만,
        // 의존 선언이 바뀌어도 규칙이 남도록 코드로도 고정한다.
        then("domain은 infrastructure에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.chobolevel.infrastructure..")
            )
        }

        then("infrastructure는 api에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.infrastructure..")
                    .should().dependOnClassesThat().resideInAPackage("com.chobolevel.api..")
            )
        }

        then("api의 main 코드는 infrastructure에 의존하지 않는다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.api..")
                    .should().dependOnClassesThat().resideInAPackage("com.chobolevel.infrastructure..")
            )
        }
    }

    given("포트(Provider 인터페이스)의 위치") {
        then("*Provider 인터페이스는 domain에만 있다") {
            check(
                classes().that().areInterfaces().and().haveSimpleNameEndingWith("Provider")
                    .should().resideInAPackage("com.chobolevel.domain..")
            )
        }
    }

    given("Redis 키 정의의 위치") {
        then("*RedisKey 객체는 domain에만 있다") {
            check(
                classes().that().haveSimpleNameEndingWith("RedisKey")
                    .should().resideInAPackage("com.chobolevel.domain..")
            )
        }
    }

    given("영속성 구현의 위치") {
        then("domain에는 Spring Data JPA·QueryDSL 리포지토리 구현이 없다") {
            check(
                noClasses().that().resideInAPackage("com.chobolevel.domain..")
                    .should().beAssignableTo(JpaRepository::class.java)
                    .orShould().beAssignableTo(QuerydslRepositorySupport::class.java)
            )
        }

        then("*RepositoryAdapter는 infrastructure에만 있다") {
            check(
                classes().that().haveSimpleNameEndingWith("RepositoryAdapter")
                    .should().resideInAPackage("com.chobolevel.infrastructure..")
            )
        }
    }
})
