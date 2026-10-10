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

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // ErrorType이 HttpStatus를 공개 프로퍼티로 노출하므로 spring-web은 domain의 공개 API의 일부다.
    // 웹이 없는 모듈(worker)이 domain에 의존해도 컴파일되도록 api로 선언한다.
    api("org.springframework:spring-web")

    // aws s3 (S3Configuration)
    implementation(libs.spring.cloud.aws)

    // envers
    implementation("org.springframework.data:spring-data-envers")

    // querydsl
    // jakarta classifier는 카탈로그에 표현할 수 없어 버전만 참조한다.
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")

    // flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // devtools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // kapt(java annotation -> kotlin annotation)
    kapt("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

