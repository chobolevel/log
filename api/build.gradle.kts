val mainClassPath = "com.chobolevel.api.ApiApplication.kt"

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set(mainClassPath)
}

plugins {

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

    // Java JWT 라이브러리
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    // XML 문서의 Java 객체 간 매핑 자동화
    implementation("javax.xml.bind:jaxb-api:2.3.1")
}
