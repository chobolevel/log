package com.chobolevel.api.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.Slice
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import com.tngtech.archunit.library.freeze.FreezingArchRule
import io.kotest.core.spec.style.BehaviorSpec

// 모듈·패키지 의존 규칙(decisions/module-and-package-structure.md)을 코드로 고정한다.
// 이미 존재하는 위반은 archunit_store에 얼려 두고(FreezingArchRule), 새 위반만 막는다.
// 재편 단계에서 위반을 고치면 저장소 항목이 사라지므로, 허용 목록은 줄어드는 방향으로만 바뀐다.
class DependencyRuleTest : BehaviorSpec({

    val classes: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.chobolevel")

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
                        "okhttp3.."
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
})
