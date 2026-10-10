import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// gradle tasks 사용 목록
plugins {
    val kotlinVersion = "1.8.21"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    id("org.springframework.boot") version "3.1.0" apply false
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("com.google.cloud.tools.jib") version "3.4.4" apply false
}

// subprojects {} 안에서는 타입 안전 접근자 `libs`가 서브프로젝트 기준으로 해석되어 실패하므로, 루트에서 미리 잡아 둔다.
val catalog = libs

// 프로젝트에 있는 모든 모듈 관리
allprojects {
    group = "com.chobolevel"
    version = "0.0.1-SNAPSHOT"

    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    ktlint {
        filter {
            exclude("*.kts")
            exclude("**/generated/**")
        }
    }
}

// 프로젝트 하위에 있는 모듈 관리(settings.gradle 파일 내 include 모듈)
subprojects {

    apply {
        plugin("kotlin")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.plugin.spring")
    }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    ext {
        set("springBootVersion", "3.1.0")

        set("baseJvmFlags", { memory: String, imageTag: String?, stage: String? ->
            listOf(
                "-server",
                "-Xms$memory",
                "-Xmx$memory",
                "-XX:+UseContainerSupport",
                "-Dspring.profiles.active=$stage",
                "-XX:+UseStringDeduplication",
                "-Dfile.encoding=UTF8",
                "-Dsun.net.inetaddr.ttl=0",
                "-Dtag=$imageTag",
                "-Djasypt.encryptor.password=chobolevel"
            )
        })
        set("dockerEnv", { stage: String?, port: String, applicationUser: String ->
            mapOf(
                "SPRING_PROFILES_ACTIVE" to stage,
                "TZ" to "Asia/Seoul",
                "PORT" to port,
                "APPLICATION_USER" to applicationUser
            )
        })
        set("dockerUser", "nobody")
        set("containerCreationTime", "USE_CURRENT_TIMESTAMP")
        set("dockerBaseImage", "eclipse-temurin:17.0.6_10-jdk-alpine")
    }

    // 모듈별 런타임 의존성은 각 모듈의 build.gradle.kts에서 선언한다. 여기에는 모든 모듈에 공통인 테스트 도구만 둔다.
    dependencies {
        // test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation(catalog.kotest.runner.junit5)
        testImplementation(catalog.kotest.assertions.core)
        testImplementation(catalog.kotest.property)
        testImplementation(catalog.kotest.framework.datatest)
        testImplementation(catalog.kotest.extensions.spring)
        testImplementation(catalog.mockk)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
