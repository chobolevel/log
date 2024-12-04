import org.jetbrains.kotlin.ir.backend.js.compile

val mainClassPath = "com.chobolevel.api.ApiApplicationKt"

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set(mainClassPath)
}

plugins {
    id("com.google.cloud.tools.jib")
}

dependencies {
    api(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-web")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // spring doc(register of doc)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // jasypt
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")

    // Java JWT 라이브러리
    implementation("io.jsonwebtoken:jjwt:0.9.1")

    // XML 문서의 Java 객체 간 매핑 자동화
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    // logger
    implementation("com.github.napstr:logback-discord-appender:1.0.0")

    // actuator + micrometer
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    // websocket(stomp)
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // websocket security
    implementation("org.springframework.security:spring-security-messaging")
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
