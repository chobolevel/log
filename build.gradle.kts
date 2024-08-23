import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.10"))
    }
}

group = "com.chobolevel"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

// gradle tasks 사용 목록
plugins {
    val kotlinVersion = "1.8.21"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    id("org.springframework.boot") version "3.1.0" apply false
    id("io.spring.dependency-management") version "1.1.0"
    id("com.avast.gradle.docker-compose") version "0.16.11"
    id("org.flywaydb.flyway") version "7.13.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1" apply false
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.3.1"
    id("com.google.cloud.tools.jib") version "3.1.2" apply false
}

// 프로젝트에 있는 모든 모듈 관리
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
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
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    val kotestVersion = "5.5.5"
    val jacksonVersion = "2.14.2"

    java.sourceCompatibility = JavaVersion.VERSION_17

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

    dependencies {
        // starter
        implementation("org.springframework.boot:spring-boot-starter-data-redis")

        // jackson
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
        implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")

        // 코틀린 내장 기능 외의 고수준 API 사용하기 위해 의존성 추가
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

        // aws s3
        implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

        // test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.kotest:kotest-runner-junit5:${kotestVersion}")
        testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
        testImplementation("io.kotest:kotest-property:${kotestVersion}")
        testImplementation("io.kotest:kotest-framework-datatest:${kotestVersion}")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
        testImplementation("io.mockk:mockk:1.13.4")
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

dependencies {
    implementation(kotlin("stdlib"))
}
