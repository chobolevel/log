# infrastructure 모듈 신설 (모듈 재편 5단계)

- 상태: 확정
- 작성일: 2026-10-17
- 관련: [모듈·패키지·의존 구조 재편 ADR](../../decisions/module-and-package-structure.md) (5단계), 후속 6단계(worker 모듈)

## 배경과 목표

`domain`에는 Repository 구현(Adapter, Spring Data JPA, QueryDSL)과 S3 설정이, `api`에는 Redis·Redisson·Resend·BCrypt 구현과 Kafka·Redis 설정이 섞여 있다. 6단계에서 `worker`를 만들면 `worker`는 `api`에 의존할 수 없으므로, 둘이 함께 쓰는 기술 구현이 `api` 안에 있으면 안 된다.

끝나면 다음이 가능해진다.
- 외부 기술(DB 접근, Redis, 메일, S3, Kafka 토픽 설정)의 구현이 `infrastructure` 한 곳에 모이고, `api`와 `worker`는 포트(인터페이스)만 안다.
- `api`가 `infrastructure`의 클래스를 컴파일 시점에 볼 수 없어서(`runtimeOnly`) 경계가 빌드로 강제된다.
- 6단계에서 `worker`를 같은 방식으로 붙일 수 있다.

외부에서 보이는 동작은 바뀌지 않는다: HTTP API, Redis 키 문자열과 TTL, DB 스키마, Kafka 이벤트 페이로드가 모두 그대로다.

## 결정 사항

| 결정 | 선택 | 대안 | 선택 이유 |
|---|---|---|---|
| `api` → `infrastructure` 의존 | `runtimeOnly` (테스트는 `testImplementation`) | `implementation` | 컴파일 시점에 경계가 강제된다. 정석적인 의존 역전 형태 |
| 포트(Provider 인터페이스) 위치 | `domain/common/provider` | `infrastructure`에 인터페이스와 구현을 함께 / 별도 `application` 모듈 | `api`·`worker`가 공유해야 하고 `infrastructure`는 `api`에 의존할 수 없다. `application` 모듈은 7단계로 보류 |
| 캐시 키 정의 위치 | `domain` 기능별 `vo` (이전 결정 "infrastructure"를 수정) | `infrastructure` / `api` 유지 | `runtimeOnly`에서는 `api`가 `infrastructure`의 키를 import할 수 없고, `worker`의 consumer도 같은 키를 쓴다 |
| Kafka 토픽 이름 상수 | `domain`으로 (`KafkaTopicConfiguration`의 `companion object` 8개) — 토픽 빈 정의만 `infrastructure` | 전부 `infrastructure` | relay 스케줄러와 consumer(지금은 `api`, 6단계에서 `worker`)가 컴파일 시점에 토픽 이름을 쓰고, `runtimeOnly`에서는 `infrastructure`를 볼 수 없다 |
| `S3Properties` | `api`에 유지 | `infrastructure`로 이동 | `UserImageConverter`·`SubjectImageConverter`가 컴파일 시점에 쓴다 (`S3Configuration`은 `@Value`로 자격 증명만 읽으므로 이동 가능) |
| `infrastructure` 패키지 | 영속성은 `domain` 애그리거트 구조를 미러, 나머지는 기술별(`redis`, `kafka`, `email`, `s3`, `password`) | 전부 기술별 | 영속성 이동이 패키지 접두어 교체로 끝난다 |
| `BCryptPasswordProvider` | `infrastructure`로 이동 (`PasswordProvider` 포트는 `domain`) | `api`에 유지 | Provider 구현을 한 곳에 모아 일관성을 유지한다 |
| 테스트 위치 | `api/src/test` 유지 | `infrastructure`로 이동 후 CI 확장 | CI(`:api:test`), Dummy, Testcontainers를 그대로 쓴다. 전용 테스트 소스는 6단계에서 필요할 때 만든다 |
| 스모크 테스트의 DB와 스키마 검증 | 기존 `AbstractMySQLContainerTest`(MySQL 8.0, Flyway 적용)를 재사용하고 `ddl-auto: validate`로 엔티티-스키마 일치까지 검증. 이미 어긋난 곳이 드러나면 이 설계 안에서 고치지 않고 별도 발견 사항으로 분리하며, 그동안은 `none`으로 둔다 | H2 + `create-drop` | 마이그레이션 SQL이 MySQL 문법이라 H2에서는 Flyway를 쓸 수 없고, 운영과 같은 조건의 기동을 검증하는 것이 목적이다. 컨테이너는 JVM당 하나라 기동 시간이 늘지 않는다. 기존 H2 슬라이스 테스트는 그대로 둔다 |
| MySQL 컨테이너 공유 방식 | 컨테이너를 `MySQLTestContainer` 객체로 분리하고 `AbstractMySQLContainerTest`는 여기에 위임 (구현 중 변경) | `AbstractMySQLContainerTest` 상속 | 상속하면 슬라이스 테스트용 `ddl-auto=none`이 서브클래스의 `validate`보다 우선해서 스키마 검증이 적용되지 않는다(변이 검증으로 확인). 기동 시점은 JVM당 한 번으로 같다 |
| 빈 누락 시 실패 분석 | 테스트 전용 `FastFailureAnalyzer`(`META-INF/spring.factories`)로 Boot의 `NoSuchBeanDefinitionFailureAnalyzer`를 대체 (구현 중 추가) | 기본 분석기 유지 / Gradle 테스트 전체 타임아웃 | 기본 분석기가 이 앱의 빈 그래프에서 수 분 이상 걸려 배선이 깨졌을 때 CI가 매달린다. 대체하면 27초 안에 실패한다. 대신 `api` 테스트 전체에서 빈 누락 진단 메시지가 단순해진다 |
| 스모크 테스트의 설정 복호화 | `ENC(...)` 값은 테스트용 프로퍼티로 필요한 항목만 평문 더미로 덮어쓴다 | 운영 jasypt 비밀번호를 테스트에서 사용 | 운영 비밀번호가 테스트 코드에 들어가지 않게 한다 |
| 선행 안전망 | 컨텍스트 스모크 테스트(MySQL+Redis 컨테이너, Kafka 미연결)와 Provider 계약 테스트를 이동 전에 먼저 추가 | 안전망 없이 진행 | 빈 스캔·JPA 스캔이 모듈 경계를 넘어 바뀌는 것이 가장 큰 위험인데, 지금은 컨텍스트 전체를 띄우는 테스트가 없다 |

## 영향 범위

**새로 만든다**
- `infrastructure` 모듈: `settings.gradle.kts`, `infrastructure/build.gradle.kts`, `InfrastructureConfigurationLoader` (`AutoConfiguration.imports` 등록, `@ComponentScan` + `@EnableJpaRepositories` + `@ConfigurationPropertiesScan`)
- `api/build.gradle.kts`: `runtimeOnly(project(":infrastructure"))`, `testImplementation(project(":infrastructure"))`

**`domain` 안의 구현을 `infrastructure`로 (패키지 미러)**
- Repository 구현: Adapter 17, Spring Data JPA 17, Querydsl 7 (`QueryFilter`·정렬 번역은 4.5단계에서 이미 Querydsl 리포지토리 안으로 이동함)
- `AuditConfiguration`(`@EnableJpaAuditing`), `S3Configuration`
- `DomainConfigurationLoader`는 `@EntityScan`만 남기고, `@EnableJpaRepositories`와 `@ComponentScan`은 `infrastructure` 로더로 옮긴다

**`api`에서 `infrastructure`로**
- Redis: `RedisCacheProvider`, `RedissonDistributedLockProvider`, `RedisConfiguration`, `RedisProperties`(안에 있는 미사용 `RedisData` 빈은 삭제)
- Email: `ResendEmailProvider`, `ResendProperties`
- Password: `BCryptPasswordProvider`
- S3: `S3Configuration`(위 `domain`에서 이동). `S3Properties`는 `api` 변환기가 쓰므로 `api`에 남고, `UploadService`도 `AmazonS3` 타입만 쓰므로 `api`에 남는다
- Kafka: `KafkaTopicConfiguration`의 토픽 빈 정의 8개 (relay 스케줄러·consumer는 6단계)

**`api`에서 `domain`으로**
- 포트 4종: `CacheProvider`, `DistributedLockProvider`, `EmailProvider`, `PasswordProvider` → `domain/common/provider`
- `CacheKeyPrefix` 12개 키 + TTL 상수 2개 → 기능별 키 정의(`record/like`, `record/view`, `user/follow`, `user` 등의 `vo`)
- Kafka 토픽 이름 상수 8개(`KafkaTopicConfiguration.companion`) → `domain`의 이벤트 토픽 정의(`vo`). 사용처는 consumer·relay 스케줄러 8개 클래스

**건드리지 않는 외부 계약**: HTTP API, Redis 키 문자열·TTL, DB 스키마(Flyway는 `domain` 유지), 이벤트 페이로드, `application.yml`의 설정 키 이름.

**테스트 인프라(S-1에서 추가)**: `MySQLTestContainer`, `RedisTestContainer`, `smoke/ApplicationContextSmokeTest`, `smoke/FastFailureAnalyzer`와 `src/test/resources/META-INF/spring.factories`. 후자는 `api` 테스트 클래스패스 전체에 적용된다.

**테스트 쪽 변경**: 포트·키·Adapter를 import하는 기존 테스트의 import 수정, `DomainJpaTestConfig`의 스캔 기준 패키지에 `infrastructure` 추가, `api`에서 Adapter를 직접 쓰는 테스트를 위한 `testImplementation`.

## 슬라이스

| # | 동작 변화 | 영향 모듈/패키지 | 위험도 |
|---|---|---|---|
| S-1 | 컨텍스트 스모크 테스트 추가 (현재 구조에서 먼저 통과) | `api` 테스트 | 중간 |
| S-2 | Provider 계약 특성화 테스트 추가 (Redis 캐시, Redisson 락, BCrypt) | `api` 테스트 | 중간 |
| S-3 | `infrastructure` 모듈 골격과 의존 규칙(빈 모듈, 로더, `runtimeOnly`) | 빌드, `api`, 신규 모듈 | 중간 |
| S-4 | 포트 4종을 `domain/common/provider`로 이동 | `api`, `domain` | 낮음 |
| S-5 | `CacheKeyPrefix`를 기능별 키 정의로, Kafka 토픽 이름 상수를 이벤트 토픽 정의로 분리해 `domain`으로 (문자열 불변) | `api`, `domain` | 높음 |
| S-6 | 영속성 구현(Adapter·Jpa·Querydsl·Audit 설정)을 `infrastructure`로 | `domain`, `infrastructure` | 높음 |
| S-7 | 기술 어댑터(Redis·Email·Password·S3 설정·Kafka 토픽 빈)와 Properties를 `infrastructure`로 | `api`, `infrastructure` | 높음 |
| S-8 | 문서·규칙 정리 (`CLAUDE.md`, ADR, ArchUnit 최종 점검) | 문서, 테스트 | 낮음 |

S-1, S-2는 production 코드를 바꾸지 않는 안전망이다. 순서는 번호 순이며, 모든 슬라이스는 앞 슬라이스의 테스트(S-1, S-2 포함)를 동작 변경 없이 통과해야 한다. 안전망 테스트는 import(와 이동에 따른 테스트 설정) 수정만 허용하며 assertion은 바꾸지 않는다.

## 슬라이스별 테스트 케이스

### S-1 컨텍스트 스모크 테스트

- 통합 (`@SpringBootTest` + Testcontainers MySQL·Redis, Kafka 미연결):
  - 컨텍스트가 기동한다 (Flyway 마이그레이션이 컨테이너 MySQL에 적용되고, `ddl-auto: validate`로 엔티티 매핑이 스키마와 일치함을 확인)
  - 도메인 `*Repository` 인터페이스 빈 17개가 각각 정확히 1개씩 존재한다 (목록을 테스트에 명시하고, 도메인의 실제 `*Repository` 인터페이스를 스캔한 결과와 일치하는지도 확인해 목록이 낡는 것을 막는다)
  - 포트 4종(`CacheProvider`, `DistributedLockProvider`, `EmailProvider`, `PasswordProvider`)이 각각 빈 1개로 해석된다
  - `JwtProperties`, `SecurityProperties`, `GuestProperties`, `S3Properties`, `ResendProperties`, `FrontServerProperties`가 빈으로 등록되고 값이 바인딩된다
  - 엔티티 저장 → (flush·clear로 1차 캐시를 비운 뒤) 조회 왕복이 되고 `createdAt`/`updatedAt`이 채워진다 (JPA 스캔과 감사 설정)
  - `CacheProvider` put → get 왕복이 Redis 컨테이너에서 된다
  - `GET /`가 200을 반환하고, 보안 필터 체인이 붙인 응답 헤더가 있다 (웹 계층과 보안 필터 체인 기동)
  - `ddl-auto`가 실제로 `validate`로 적용되어 있다 (설정이 다른 값에 덮여도 알 수 있게)
- 경계 / 예외: Kafka 브로커가 없어도 컨텍스트가 기동한다 (리스너는 자동 시작을 끄고, 스케줄러는 대상 이벤트가 없어 Kafka를 호출하지 않는다). 테스트 클래스가 끝나면 컨텍스트를 종료해 스케줄러가 공유 MySQL을 계속 폴링하지 않게 한다 (`@DirtiesContext`)
- 배선이 깨졌을 때: 빈 누락·스캔 오류·엔티티-스키마 불일치가 각각 컨텍스트 기동 실패로 드러난다(변이 4건으로 확인). 기동 실패는 `FastFailureAnalyzer` 덕분에 수십 초 안에 보고된다

### S-2 Provider 계약 특성화 테스트

- 단위 / 통합 (Redis 컨테이너):
  - `CacheProvider`: `put`/`get`, TTL 지정 `put` 후 만료, `mget`이 키 순서대로 반환하고 없는 키는 `null`, `putIfAbsent`(없을 때 true / 있을 때 false, TTL 변형 포함), `increment`/`decrement`가 없는 키에서 1/-1로 시작, `delete`와 `hasKey`
  - `DistributedLockProvider`: 락 획득 후 `action` 결과를 반환, 같은 키 동시 실행이 직렬화된다, `waitTime` 안에 획득 못 하면 `InternalSystemException`(LOCK_ACQUISITION_FAILED), `action`이 예외를 던져도 락이 해제되어 다음 획득이 된다
  - `PasswordProvider`: `encode`가 평문과 다르고 매번 다른 값, `matches` 성공/실패
- 경계 / 예외: `null` 입력 처리는 현재 구현 동작을 그대로 고정한다

### S-3 모듈 골격

- 단위 (ArchUnit): `domain`은 `infrastructure`에 의존하지 않는다 / `infrastructure`는 `api`에 의존하지 않는다 / `api` main 코드는 `infrastructure`에 의존하지 않는다 (현재 위반 0건)
- 통합: S-1·S-2가 수정 없이 통과한다 (빈 모듈과 로더만 추가됨, `runtimeOnly`로 `testRuntimeClasspath`에 포함됨)
- 통합 (스모크 테스트에 추가): `InfrastructureConfigurationLoader` 빈이 런타임에 정확히 1개 로드된다. `api`는 `infrastructure`를 `runtimeOnly`로만 연결하므로 `AutoConfiguration.imports` 등록이 끊기면 컴파일은 통과해도 이 모듈의 빈이 하나도 올라오지 않는다
- 경계 / 예외: `@WebMvcTest`, `@DataJpaTest` 슬라이스 테스트가 로더의 영향을 받지 않는다 (기존 테스트 전체 통과)
- 컴파일 시점 경계: `api` main 코드가 `infrastructure` 클래스를 참조하면 컴파일이 실패한다(Gradle `runtimeOnly`). ArchUnit 3개 규칙은 의존 선언이 바뀌어도 경계가 남도록 하는 두 번째 방어선이다

### S-4 포트 이동

- 단위: 포트를 쓰는 기존 서비스 단위 테스트가 import 수정만으로 통과한다
- 통합: S-1·S-2 통과
- 경계 / 예외: 컴파일 시점에 `api` → 포트 참조가 `domain`으로 해석된다

### S-5 캐시 키·토픽 이름 분리

- 단위 (이동 전에 먼저 작성해 현재 구현으로 통과시킨다):
  - 12개 키 함수가 대표 입력에서 이동 전과 동일한 문자열을 반환한다 (기대값을 문자열로 직접 명시)
  - TTL 상수가 `RECORD_LIKE_CACHE_TTL_MINUTES = 10`, `RECORD_CONTRIBUTION_CACHE_TTL_MINUTES = 5`다
  - Kafka 토픽 이름 상수 8개(`record-like-sync-events`, `...-dlq` 등)가 이동 전과 동일한 문자열이다
- 통합: S-1 캐시 왕복, 좋아요·팔로우·조회수 서비스 단위 테스트가 통과한다
- 경계 / 예외: `email`에 `:`가 들어간 경우, `year`, 큰 id 값에서도 치환 결과가 이동 전과 같다

### S-6 영속성 구현 이동

- 단위 (ArchUnit): `domain`에 `JpaRepository`/`QuerydslRepositorySupport`를 상속한 클래스가 없다
- 통합: S-1의 `*Repository` 빈 17개 / 기존 `*RepositorySearchTest`(검색 조건·정렬 특성화), `UserJpaRepositoryTest`, `UserJpaRepositoryContainerTest`가 수정 없이(import 제외) 통과한다
- 경계 / 예외: `@Where` 기반 소프트 삭제, `createdAt` 자동 채움이 이동 후에도 동작한다 / `@EntityScan`(`domain`)과 `@EnableJpaRepositories`(`infrastructure`) 기준 패키지가 분리돼도 엔티티와 리포지토리가 모두 스캔된다

### S-7 기술 어댑터 이동

- 통합: S-1(Properties 바인딩 포함)·S-2(캐시·락·비밀번호 계약)가 수정 없이 통과한다
- 단위 (ArchUnit): `api`·`domain`이 `org.redisson`, `com.resend` 구현 클래스에 의존하지 않는다
- 경계 / 예외: `@ConfigurationPropertiesScan` 범위가 `infrastructure` 로더로 이동해도 `redis.chobolevel.*`, `resend.*`, `cloud.aws.*` 키가 동일하게 바인딩된다 / `RedissonDistributedLockProvider`·`ResendEmailProvider`의 예외 매핑(`InternalSystemException`, `ExternalSystemException`)이 유지된다

### S-8 문서·규칙 정리

- 단위 (ArchUnit): 최종 규칙 목록이 모두 통과한다
- 경계 / 예외: `CLAUDE.md`의 모듈 구조·Repository 위치 규칙이 코드와 일치한다

## 위험도와 리뷰 깊이

| # | 위험도 | 근거 | 정독 지점 |
|---|---|---|---|
| S-1 | 중간 | production 코드 변경 없는 테스트 인프라. 환경 의존(컨테이너, 설정 복호화) | 테스트 설정의 프로퍼티 오버라이드 |
| S-2 | 중간 | production 코드 변경 없음. 락 테스트의 시간 의존으로 불안정해질 수 있음 | 락 동시성 테스트의 대기·타임아웃 값 |
| S-3 | 중간 | 모듈 의존 방향과 자동 구성 등록 | `runtimeOnly` 선언, `AutoConfiguration.imports` |
| S-4 | 낮음 | 패키지 이동만 | - |
| S-5 | 높음 | Redis 캐시 정합성: 키 문자열이 바뀌면 운영 캐시가 갈라지고, 토픽 이름이 바뀌면 Outbox 이벤트가 유실된다 | 12개 키 템플릿·TTL 상수·토픽 이름 8개가 이동 전과 한 글자도 다르지 않은지, 키 정의별 소속 기능 |
| S-6 | 높음 | JPA·빈 스캔이 모듈 경계를 넘는다. 잘못되면 기동 실패 또는 리포지토리 누락 | `InfrastructureConfigurationLoader`의 스캔 범위, `DomainConfigurationLoader`에 남는 `@EntityScan`, `DomainJpaTestConfig` 기준 패키지 |
| S-7 | 높음 | 외부 연동(Redis·Redisson 분산락·Resend·S3)과 설정 바인딩 | `RedisConfiguration`의 `@Primary` 빈, 락 예외 매핑, `@ConfigurationPropertiesScan` 범위, `S3Configuration`의 프로퍼티 키, 토픽 빈 8개의 이름·파티션·복제 수가 그대로인지 |
| S-8 | 낮음 | 문서와 규칙 | - |

## 끝단 검증

S-1 컨텍스트 스모크 테스트와 S-2 Provider 계약 테스트가 모든 후속 슬라이스에서 수정 없이 통과하는 것을 기준으로 한다. 두 테스트가 CI(`./gradlew :api:test`)에서 매 PR마다 실행되므로, 자동 구성 누락·포트-구현 연결 누락·Properties 바인딩 누락이 머지 전에 드러난다. 각 슬라이스는 `ktlintCheck`와 `:api:bootJar`도 통과해야 한다.

## 비범위

- `worker` 모듈 신설과 relay 스케줄러·consumer 이동 (6단계)
- `application` 모듈 분리 (7단계, 서비스가 `api` DTO에서 벗어난 뒤)
- 서비스가 캐시 키 문자열을 조립하는 방식을 기능별 캐시 클래스(`RecordLikeCache` 등)로 감싸는 변경
- `NotificationPublisher`/`NotificationOutboxPublisher` 이동 (도메인 리포지토리만 쓰는 유스케이스 협력자라 `api`에 유지)
- 키 값·TTL 변경, 설정 키 이름 변경, Kafka 토픽·페이로드 변경
- `ResendEmailProvider`의 외부 API 호출 검증 (SDK를 직접 호출하며 이번에 코드를 바꾸지 않는다. 빈 연결은 S-1이 확인한다)
- 소프트 삭제 방식 통일, 마이그레이션 job 분리, JPA 엔티티와 도메인 모델 분리

## 열린 질문

없음.
