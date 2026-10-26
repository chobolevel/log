import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

dependencies {
    // 영속성·외부 연동 구현이 domain의 엔티티와 포트를 쓴다. 의존은 한 방향뿐이다(domain은 infrastructure를 모른다).
    // 이 모듈은 api에 의존하지 않는다. api도 이 모듈을 runtimeOnly로만 연결해 컴파일 시점에는 서로 볼 수 없다.
    api(project(":domain"))

    // Querydsl 리포지토리 구현. Q-type은 domain(kapt)이 생성하고, 여기서는 그 클래스를 쓴다. jakarta classifier는 카탈로그에 표현할 수 없어 버전만 참조한다.
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
}
