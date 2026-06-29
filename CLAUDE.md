# CLAUDE.md

이 파일은 Claude Code가 이 프로젝트에서 작업할 때 따라야 할 규칙과 컨텍스트를 담고 있다.

---

## Git 커밋 규칙

- 커밋 메시지에 Claude, Co-Authored-By, Generated with Claude Code 등 AI 관련 내용을 포함하지 않는다.
- 커밋 메시지는 변경 내용을 간결하게 한국어로 작성한다.
- 형식: `<type>: <내용>` (예: `feat: 게시글 등록 API 추가`, `fix: 토큰 만료 예외 처리`)

---

## 프로젝트 개요

- **프로젝트명**: log-be (개인 블로그 백엔드)
- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.1.0
- **멀티 모듈 구조**:
  - `domain` — 엔티티, Repository, Finder, QueryFilter 등 도메인 핵심 로직
  - `api` — Controller, Service, Converter, Validator, Updater, DTO 등 API 계층
- **빌드 도구**: Gradle (Kotlin DSL)
- **테스트**: Kotest + MockK
- **코드 포맷터**: ktlint

---

## 코딩 스타일 규칙

### 1. 타입 명시 (Type Declaration)

Kotlin은 타입 추론을 지원하지만, **이 프로젝트에서는 타입을 명시하는 것을 원칙으로 한다.**

> 근거: *Effective Kotlin* — 타입 추론에 의존하면 IDE 없이 코드를 읽을 때(PR 리뷰, 코드 탐색 등) 타입 파악이 어렵다.

**타입을 반드시 명시해야 하는 곳**

```kotlin
// 클래스 프로퍼티
private val repository: PostRepository = mockk()
private val redisTemplate: RedisTemplate<String, PostResponseDto> = mockk()

// 컬렉션/제네릭
val posts: List<Post> = listOf(DummyPost.toEntity())
val postResponses: List<PostResponseDto> = listOf(DummyPost.toResponseDto())

// 반환 타입이 명확해야 하는 함수
fun findById(id: Long): Post { ... }
```

**타입 생략이 허용되는 곳**

```kotlin
// 생성 함수명에서 타입이 명백한 경우
val postId = DummyPost.ID          // Long임이 명백
val totalCount = 1L                 // 리터럴로 타입이 명백

// 람다 파라미터, for 루프 변수
warmers.forEach { it.warm(...) }
for (post in posts) { ... }
```

### 2. 패키지 구조 (도메인 기준)

레이어 기준이 아닌 **도메인 기준**으로 패키지를 구성한다.

```
api/
  post/
    controller/
    service/
    converter/
    dto/
    validator/
    updater/
domain/
  post/
    Post.kt
    PostFinder.kt
    PostRepository.kt
    PostQueryFilter.kt
```

### 3. 테스트 작성 원칙

- 서비스 계층 테스트는 외부 의존성을 모두 MockK로 대체하여 비즈니스 로직만 검증한다.
- 성공 케이스와 실패 케이스를 반드시 함께 작성한다.
- 의존성은 `@BeforeEach`에서 생성자로 직접 주입한다 (컬렉션 타입 자동 주입 불가 문제 회피).
- Dummy 객체는 `common/dummy/` 하위에 도메인별로 분리한다.

```kotlin
// 의존성 선언 시 타입 명시
private val repository: PostRepository = mockk()
private val finder: PostFinder = mockk()

// given/when/then 구조 준수
@Test
fun `게시글_등록_성공`() {
    // given
    val userId: Long = DummyUser.id
    val post: Post = DummyPost.toEntity()
    every { repository.save(post) } returns post

    // when
    val result: Long = postService.createPost(userId, request)

    // then
    result shouldBe DummyPost.ID
    verify { repository.save(post) }
}
```

### 4. 예시 코드 작성 시

Claude가 이 프로젝트에서 예시 코드를 작성할 때는 반드시 위의 타입 명시 규칙을 따른다. 타입 추론에 의존하는 코틀린 관용구(`val x = someFunction()`) 스타일은 사용하지 않는다.

---

## 주요 기술 스택

| 항목 | 내용 |
|------|------|
| Kotlin | 1.8.21 |
| Spring Boot | 3.1.0 |
| JVM | 17 |
| 테스트 | Kotest 5.5.5, MockK 1.13.4 |
| 캐시 | Spring Data Redis |
| ORM | Spring Data JPA |
| 포맷터 | ktlint 11.3.1 |
| 컨테이너 빌드 | Jib 3.4.4 |