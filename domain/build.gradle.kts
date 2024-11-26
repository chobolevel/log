import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
    id("org.flywaydb.flyway")
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

    // envers
    implementation("org.springframework.data:spring-data-envers")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:${queryDslVersion}:jakarta")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")

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

flyway {
    url = "jdbc:mysql://localhost:3306/log"
    user = "root"
    password = "1234"
    baselineVersion = "0"
    outOfOrder = true
}
