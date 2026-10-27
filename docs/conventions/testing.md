# 테스트 작성 규칙

## 스타일 (실제 코드 기준)

| 대상 | 방식 |
|---|---|
| Service / Validator / Updater / Consumer 등 단위 테스트 | Kotest `BehaviorSpec` + MockK (`given` / `when` / `then`) |
| Controller | `@WebMvcTest` + MockMvc (JUnit5 `@Test`) |
| Repository | `@DataJpaTest` 슬라이스 테스트, 방언에 의존하는 동작은 Testcontainers MySQL 테스트(`*ContainerTest`) |
| 아키텍처 | `DependencyRuleTest` (ArchUnit) |

## 기본 원칙

- 성공 케이스와 실패 케이스를 함께 작성한다.
- 서비스 단위 테스트는 외부 의존성을 MockK로 대체하고 비즈니스 로직만 검증한다.
- mock이 많다는 것은 설계 신호다. api 서비스는 조율자라 허용되지만, "이 서비스가 너무 많은 것을 알고 있지 않은가?"를 점검 기준으로 삼는다.
- mock과 대상 객체는 스펙 본문 상단에서 타입을 명시해 선언하고 생성자로 직접 주입한다. 타입 명시 규칙은 테스트 코드에도 동일하게 적용한다.
- 테스트 데이터는 `api/src/test/kotlin/com/chobolevel/api/common/dummy/` 아래 도메인별 Dummy 객체(Object Mother)를 쓴다. `toEntity()`는 엔티티, `toResponseDto()`는 DTO를 만든다.
- 반환값과 상태 변화를 모두 검증한다. 인수 값까지 확인해야 하면 `slot` + `capture`를 쓴다(`any()`는 호출 여부만 확인한다). Unit 반환 메서드는 `justRun`으로 mock한다.

## BehaviorSpec 주의사항

- `given` / `when` 블록 안의 코드는 스펙 초기화 시 한 번만 실행되고, `beforeEach { clearAllMocks() }`가 그때 만든 stub을 지운다. `every { }`, `val request = ...` 같은 셋업은 반드시 `then { }` 안에 작성한다.

## 슬라이스 / DB 테스트 주의사항

- H2 통과는 MySQL 통과가 아니다. 실제로 발견된 차이: `@Where`와 quoted identifiers 충돌(H2는 `CASE_INSENSITIVE_IDENTIFIERS=TRUE` 필요), `order` 같은 예약어 칼럼, `LIKE` 대소문자 구분. 방언에 의존하는 동작은 Testcontainers MySQL로 검증한다.
- Testcontainers는 Singleton Container 패턴(`AbstractMySQLContainerTest`, `companion object` + `@JvmField`)으로 JVM당 하나만 기동하고, 포트는 `@DynamicPropertySource`로 주입한다.
- 멀티 모듈 `@DataJpaTest`는 `PersistenceTestConfig`(`@EnableJpaRepositories`는 `infrastructure`, `@EntityScan`은 `domain`)를 사용한다. 엔티티는 `domain`에, Spring Data 리포지토리 구현은 `infrastructure`에 있어서 `@DataJpaTest`의 기본 스캔(`@SpringBootApplication` 패키지인 `com.chobolevel.api`)에 잡히지 않기 때문이다. `DomainConfigurationLoader`·`InfrastructureConfigurationLoader`를 그대로 import하면 JPA와 무관한 빈(`S3Configuration` 등)까지 컴포넌트 스캔으로 함께 올라와 슬라이스가 커지므로 쓰지 않는다.
- `@WebMvcTest`는 `@EnableWebSecurity`를 로드하지 않으므로 `TestSecurityConfig`로 FilterChain을 직접 정의하고, 인증이 필요하면 `@WithMockUser(username = "1")`를 쓴다.
- `entityManager.clear()`는 REMOVED 상태의 엔티티까지 지워서 DELETE SQL이 실행되지 않는다. 삭제 검증은 `count()` 같은 조회 쿼리가 flush를 일으키게 두고 확인한다.
- `@Async` 메서드는 Spring 컨텍스트 없는 단위 테스트에서 동기로 실행된다. 실제 비동기 동작은 슬라이스/통합 테스트에서 확인한다.

## 실행

```bash
./gradlew :api:test                                                                          # 전체 (Testcontainers 때문에 Docker 필요)
./gradlew :api:test --tests "com.chobolevel.api.user.validator.UserBusinessValidatorTest"    # 특정 클래스
./gradlew :api:test --rerun-tasks                                                            # 캐시 무시
```

리포트: `api/build/reports/tests/test/index.html`

## 아키텍처 테스트

`DependencyRuleTest`는 `FreezingArchRule`로 기존 위반을 `api/src/test/resources/archunit_store`에 고정하고 새 위반만 실패시킨다. 위반을 고치면 해당 항목이 자동으로 사라진다.
