# CLAUDE.md

이 파일은 Claude Code가 이 프로젝트에서 작업할 때 따라야 할 규칙과 컨텍스트를 담고 있다.

---

## Git 커밋 규칙

- 커밋 메시지에 Claude, Co-Authored-By, Generated with Claude Code 등 AI 관련 내용을 포함하지 않는다.
- 커밋 메시지는 변경 내용을 간결하게 한국어로 작성한다.
- 형식: `<type>: <내용>` (예: `feat: 게시글 등록 API 추가`, `fix: 토큰 만료 예외 처리`)

### 커밋 진행 흐름

작업 완료 후 커밋은 아래 순서로 진행한다. **사용자 확인 전에 절대 커밋하지 않는다.**

1. `git add .` 로 변경 파일을 스테이징한다.
2. 변경 내용을 바탕으로 커밋 메시지 초안을 작성하여 사용자에게 제안한다.
3. 사용자가 확인(또는 메시지 수정)하면 그때 커밋을 실행한다.

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

### 2. 패키지 구조 (도메인 > 서브도메인 > 레이어)

최상위는 도메인, 그 아래 서브도메인, 그 아래 레이어 순서로 구성한다.

**domain 모듈** — 각 도메인/서브도메인 내에 `entity/`, `repository/`, `vo/` 레이어 패키지를 추가한다. Finder(도메인 서비스)는 레이어 구분 없이 도메인 루트에 둔다.

```
domain/
  post/
    entity/        ← JPA 엔티티 및 관련 enum
      Post.kt
      PostOrderType.kt (Post.kt 내부 정의)
    repository/
      PostRepository.kt
      PostCustomRepository.kt
    vo/
      PostQueryFilter.kt
    PostFinder.kt  ← 도메인 서비스는 루트
    comment/       ← 서브도메인
      entity/
        PostComment.kt
      repository/
        PostCommentRepository.kt
      vo/
        PostCommentQueryFilter.kt
      PostCommentFinder.kt
    image/
      entity/
        PostImage.kt
```

**api 모듈** — 서브도메인은 별도 디렉토리로 분리하고, 그 안에서 레이어를 나눈다.

```
api/
  post/
    controller/    ← Post 관련 레이어
    service/
    dto/
    converter/
    comment/       ← PostComment 서브도메인
      controller/
      service/
      dto/
      converter/
      validator/
      updater/
    image/         ← PostImage 서브도메인
      converter/
      dto/
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

### 4. 엔티티를 API 응답으로 직접 반환 금지

JPA 엔티티를 Jackson이 직렬화하면 양방향 연관관계로 인한 **무한 참조(StackOverflowError)**가 발생할 수 있다.

> 근거: `User.profileImage → UserImage.user → User.profileImage → ...` 처럼 JPA는 양방향 관계를 메모리 상에서 두 객체가 서로를 참조하는 형태로 유지한다. Jackson은 이 참조가 순환인지 모르고 끝날 때까지 객체 그래프를 탐색하기 때문에 무한 루프에 빠진다.

**반드시 Converter를 통해 DTO로 변환한 뒤 반환한다.**

```kotlin
// 잘못된 예 — 엔티티 직접 반환
return PagingResponse(data = users)

// 올바른 예 — DTO 변환 후 반환
return PagingResponse(data = converter.convert(entities = users))
```

DTO는 연관 엔티티를 다시 상위 DTO로 참조하지 않으므로 순환이 끊긴다.

### 5. 예시 코드 작성 시

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

# 테스트 코드 학습 컨텍스트 (Java/Kotlin + Spring Boot)

> 이 파일을 프로젝트 루트의 `CLAUDE.md`에 붙여넣거나, Claude Code 세션 시작 시 그대로 프롬프트로 붙여넣어 사용한다.

## 배경

이미 개발이 진행된 개인 프로젝트에 테스트 코드를 단계적으로 리트로핏(retrofit)한다. 목표는 단순히 테스트를 "채워 넣는" 것이 아니라, 대기업 백엔드 조직의 코드 리뷰 기준을 통과할 수준의 테스트 작성 역량을 기르는 것이다.

## Claude Code에게 요청하는 역할

- 답을 바로 완성해서 주지 말고, 각 단계마다 "왜 이 테스트가 필요한지 → 어떻게 구조를 잡을지 → 실제 코드" 순서로 설명하며 진행한다.
- 매 단계 진입 전, 현재 프로젝트 구조(컨트롤러/서비스/리포지토리/도메인)를 먼저 훑어보고 어떤 클래스부터 테스트할지 제안한다.
- 테스트 코드 작성 후에는 실무 코드 리뷰어 관점에서 자체 리뷰 코멘트를 함께 남긴다 (네이밍, 가독성, 불필요한 mocking, 경계값 누락 등).
- 한 번에 여러 단계를 몰아서 처리하지 않는다. 사용자가 "다음 단계"라고 명시할 때까지 현재 단계에 머문다.
- 진행 상황은 아래 체크리스트를 갱신하는 방식으로 트래킹한다.

## 진행 체크리스트

- [~] 1단계: 단위 테스트 기본기 (진행 중)
- [ ] 2단계: Spring 슬라이스 테스트
- [ ] 3단계: Testcontainers 기반 통합 테스트
- [ ] 4단계: 테스트 전략 & 커버리지 & CI
- [ ] 5단계: 계약 테스트 / E2E

---

## 1단계: 단위 테스트 기본기

**목표**: 순수 로직(도메인 객체, 서비스의 비즈니스 규칙)에 대한 단위 테스트 작성 습관화.

**다룰 내용**
- JUnit5 기본 구조 (`@Test`, `@BeforeEach`, `@DisplayName`, `@Nested`)
- AssertJ(Java) / Kotest matcher(Kotlin)로 가독성 있는 검증
- Mockito(Java) / MockK(Kotlin)로 협력 객체 mocking, `verify`/`argumentCaptor` 활용
- given-when-then 구조와 테스트 네이밍 규칙 정립 (예: `주문을_취소하면_상태가_CANCELLED로_변경된다`)
- 과도한 mocking 지양 — mock이 3개 이상 필요하면 설계(의존성 과다)를 의심

**실습 방식**: 프로젝트에서 도메인 로직이 있는 서비스 클래스 1~2개를 골라 실제로 테스트를 짜본다. Claude Code는 후보 클래스를 스캔해서 우선순위를 제안한다.

**완료 기준**: 서비스 계층 핵심 로직에 대해 given-when-then 구조의 단위 테스트를 스스로 작성할 수 있고, mock 사용 이유를 설명할 수 있다.

### 1단계 진행 현황

**작성 완료된 테스트 파일**
- `api/src/test/kotlin/com/chobolevel/api/common/dummy/DummyUser.kt` — 테스트용 User 더미 객체
- `api/src/test/kotlin/com/chobolevel/api/user/validator/UserBusinessValidatorTest.kt` — `UserBusinessValidator` 단위 테스트 (9개 케이스, 전부 통과)

**이번 세션에서 배운 것**

1. **Kotest BehaviorSpec 실행 모델 주의사항**
   - `given` / `when` 블록 안의 코드(예: `every { }`)는 스펙 초기화 시 **한 번만** 실행된다.
   - `beforeEach { clearAllMocks() }`가 그 stubs를 지워버리기 때문에 실제 테스트 실행 시점엔 mock이 비어 있다.
   - **규칙**: `every { }`, `val request = ...` 등 셋업 코드는 반드시 `then { }` 블록 안에 작성한다.

2. **Dummy 객체 위치**: `api/src/test/kotlin/com/chobolevel/api/common/dummy/` 하위에 도메인별로 분리한다.

3. **테스트 실행 명령**
   ```bash
   ./gradlew :api:test                                         # 전체 테스트
   ./gradlew :api:test --tests "com.chobolevel.api.user.validator.UserBusinessValidatorTest"  # 특정 클래스
   ./gradlew :api:test --rerun-tasks                           # 캐시 무시하고 강제 재실행
   ```
   결과 리포트: `api/build/reports/tests/test/index.html`

**작성 완료된 테스트 파일 (추가)**
- `api/src/test/kotlin/com/chobolevel/api/user/service/UserServiceTest.kt` — `UserService` 단위 테스트 (6개 케이스, 전부 통과)

**이번 세션에서 추가로 배운 것**

3. **Object Mother 패턴** (`DummyUser`)
   - 테스트마다 엔티티를 직접 생성하면 생성자 변경 시 모든 테스트 수정 필요 → 한 곳에 모아두는 패턴
   - `toEntity()`: 엔티티 생성, `toResponseDto()`: DTO 생성 — 테스트 성격에 맞게 분리

4. **justRun**: Unit(void) 반환 메서드를 mock할 때 사용 (`every { } returns Unit` 대신)

5. **반환값 + 상태 변화 모두 검증**
   - `resignUser`: `result shouldBe true` (반환값) + `user.resigned shouldBe true` (사이드 이펙트)
   - `changePassword`: `user.password shouldBe encodedNewPassword` (상태 변화)

6. **mock 5개 = 설계 신호**: 서비스가 많은 협력 객체를 가질 때 자연히 발생. API 계층 조율자 역할이므로 허용되지만, "이 서비스가 너무 많은 것을 알고 있지 않은가?" 체크 기준으로 삼는다.

**다음 할 일 (1단계 계속 또는 2단계 진입)**
- `AuthService` 단위 테스트 작성 — mock 1개라 구조 연습에 적합 (선택)
- 또는 2단계(Spring 슬라이스 테스트)로 진입

---

## 2단계: Spring 슬라이스 테스트

**목표**: 전체 컨텍스트를 매번 띄우지 않고 계층별로 필요한 범위만 테스트하는 감각 습득.

**다룰 내용**
- `@WebMvcTest` — 컨트롤러 계층, `MockMvc`로 요청/응답 검증, `@MockBean`/`@MockkBean`으로 서비스 계층 격리
- `@DataJpaTest` — 리포지토리 계층, 쿼리 메서드/QueryDSL 검증, 임베디드 DB 또는 실제 DB 선택 기준
- `@SpringBootTest`는 언제만 쓰는지 (통합 시나리오, 컨텍스트 로딩 비용 트레이드오프)
- 슬라이스 테스트에서 자주 하는 실수: 매번 `@SpringBootTest`로 때우기, 슬라이스 범위 밖 빈 주입 실패 등

**실습 방식**: 1단계에서 테스트한 서비스를 사용하는 컨트롤러와 리포지토리에 대해 각각 슬라이스 테스트 작성.

**완료 기준**: 어떤 테스트 상황에 어떤 슬라이스 어노테이션을 써야 하는지 스스로 판단할 수 있다.

---

## 3단계: Testcontainers 기반 통합 테스트

**목표**: 인메모리 DB가 아닌 프로덕션과 동일한 인프라(DB, 캐시, 메시지 큐)로 테스트.

**다룰 내용**
- Testcontainers 기본 세팅 (`@Testcontainers`, `@Container`, `DynamicPropertySource`)
- 프로젝트에서 쓰는 DB(MySQL/PostgreSQL 등)로 전환, H2와의 SQL 방언 차이 문제 확인
- Redis, Kafka 등 프로젝트에 실제로 쓰이는 인프라가 있다면 확장
- 테스트 실행 속도 문제와 컨테이너 재사용 전략 (`Singleton Container` 패턴)

**실습 방식**: 2단계 `@DataJpaTest`를 Testcontainers 기반으로 전환.

**완료 기준**: 로컬 DB 방언 차이로 인한 버그를 통합 테스트에서 잡아낸 경험을 만든다.

---

## 4단계: 테스트 전략, 커버리지, CI

**목표**: "얼마나 테스트했는가"가 아니라 "무엇을 테스트해야 하는가"를 판단하는 기준 정립.

**다룰 내용**
- Jacoco로 커버리지 측정, 수치보다 경로(정상/예외/경계값/동시성) 기준으로 우선순위 판단
- GitHub Actions 등 CI에 테스트 단계 통합, PR 시 자동 실행
- 테스트 피라미드 관점에서 프로젝트의 현재 테스트 분포 점검 (단위:슬라이스:통합 비율)
- 짧은 TDD 실습(kata)으로 사이클(Red-Green-Refactor) 체득

**실습 방식**: 프로젝트 CI 워크플로우에 테스트 실행 단계 추가, 커버리지 리포트 생성.

**완료 기준**: 커버리지 리포트를 보고 어떤 영역이 위험한지 스스로 짚어낼 수 있다.

---

## 5단계: 계약 테스트 / E2E

**목표**: 서비스 경계를 넘는 신뢰성 확보 (여유가 있을 때 진행, 필수 아님).

**다룰 내용**
- Spring Cloud Contract 또는 Pact로 소비자-제공자 계약 테스트
- RestAssured로 API E2E 테스트 시나리오 작성
- 프로젝트가 외부 API를 호출한다면 WireMock으로 stub 처리

**완료 기준**: 서비스 간 인터페이스 변경이 계약 테스트에서 감지되는 흐름을 경험한다.
