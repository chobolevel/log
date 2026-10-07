import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
    id("kotlin-allopen")
}

allOpen {
    annotations("jakarta.persistence.Entity")
    annotations("jakarta.persistence.MappedSuperclass")
    annotations("jakarta.persistence.Embeddable")
}

val queryDslVersion: String = "5.0.0"

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    // ErrorType이 HttpStatus를 공개 프로퍼티로 노출하므로 spring-web은 domain의 공개 API의 일부다.
    // 웹이 없는 모듈이 domain에 의존해도 컴파일되도록 api로 선언한다. (모듈 구조 재편 시 함께 재검토)
    api("org.springframework:spring-web")

    // email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // envers
    implementation("org.springframework.data:spring-data-envers")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:${queryDslVersion}:jakarta")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")

    // flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // devtools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // rest template(okhttp)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // kapt(java annotation -> kotlin annotation)
    kapt("com.querydsl:querydsl-apt:${queryDslVersion}:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

