val mainClassPath = "com.chobolevel.api.ApiApplicationKt"

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set(mainClassPath)
}

plugins {
    id("com.google.cloud.tools.jib")
}

dependencies {
    api(project(":domain"))

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // jackson
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.databind)

    // aws s3
    implementation(libs.spring.cloud.aws)

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // spring doc(register of doc)
    implementation(libs.springdoc.webmvc.ui)

    // jasypt
    implementation(libs.jasypt.starter)

    // Java JWT 라이브러리
    implementation(libs.jjwt)

    // XML 문서의 Java 객체 간 매핑 자동화
    implementation(libs.jaxb.api)

    // actuator + micrometer
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    // tsid
    implementation(libs.hypersistence.utils)

    // thymeleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // jsoup for crawling
    implementation(libs.jsoup)

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // kafka
    implementation("org.springframework.kafka:spring-kafka")

    // redisson: 분산 락
    implementation(libs.redisson.starter)

    // Resend(sending email SDK)
    implementation(libs.resend)

    // test: @WebMvcTest에서 MockK 기반 Bean 등록 지원
    testImplementation(libs.springmockk)

    // test: @DataJpaTest용 인메모리 DB
    testRuntimeOnly("com.h2database:h2")

    // test: @DataJpaTest에서 QueryDSL Q타입 직접 사용 시 필요
    // domain 모듈의 querydsl-jpa는 implementation이라 테스트 classpath에 전파되지 않는다.
    testImplementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")

    // test: 패키지·모듈 의존 규칙 검증 (decisions/module-and-package-structure.md)
    testImplementation(libs.archunit)

    // test: Spring Security MockMvc 지원 (@WithMockUser 등)
    testImplementation("org.springframework.security:spring-security-test")

    // test: Testcontainers (대규모 통합 테스트용 실제 컨테이너)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}

val baseJvmFlags: (memory: String, imageTag: String?, stage: String?) -> List<String> by ext
val dockerEnv: (stage: String?, port: String, applicationUser: String) -> Map<String, String> by ext
val dockerUser: String? by ext
val containerCreationTime: String? by ext
val dockerBaseImage: String? by ext
val stage: String? by project
val imageTag: String? by project
val containerImage: String? by project
val port: String = "9565"
val serviceName: String = "chobolevel-log-api"

jib {
    from {
        image = dockerBaseImage
    }

    to {
        image = "$containerImage"
        tags = setOf(imageTag, "latest")
    }
    container {
        val memory = when (stage) {
            "production" -> "2g"
            "alpha" -> "2g"
            else -> "2g"
        }
        jvmFlags = baseJvmFlags(memory, imageTag, stage)

        environment = dockerEnv(stage, port, serviceName)
        mainClass = mainClassPath
        user = dockerUser
        ports = listOf(port)
        creationTime = containerCreationTime
    }
}
