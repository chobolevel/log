# ADR 002: 모듈·패키지·의존 구조

## 상황(Context)

> 모듈은 `domain`, `api` 두 개였다.
> - `domain`: 엔티티, Repository 인터페이스와 구현체(JPA + QueryDSL), QueryFilter, 예외/에러코드, Flyway 마이그레이션
> - `api`: Controller, Service, Converter, Validator, DTO, Kafka Consumer·Relay 스케줄러, Redis·메일 등 외부 연동 구현, Security, SSE
>
> 도메인 규칙과 외부 기술 구현이 섞여 있어서, 어떤 코드가 어디까지 의존해도 되는지를 구조로 말할 수 없었다. 이 ADR은 **책임 단위로 모듈을 나누고 의존 방향을 고정하는 것**만 다룬다. 프로세스 분리(worker)와 멀티 인스턴스 확장은 이 ADR의 범위가 아니며 별도 결정으로 다룬다.

## 문제(Problem)

> 1. 외부 시스템 연동(Kafka, Redis, 메일, S3, DB 접근)의 구현이 `api`와 `domain`에 흩어져 있어서, 도메인 규칙과 기술 구현의 경계가 드러나지 않고 구현 교체와 테스트의 경계도 불분명하다.
> 2. `domain`이 웹·메일·HTTP 클라이언트 같은 도메인과 무관한 기술에 의존하고, `QueryFilter`가 QueryDSL 생성 코드(Q-type)에 묶여 Repository 인터페이스까지 구현 기술을 드러낸다.
> 3. `common` 패키지가 잡동사니 통이 되고, 서브 기능 패키지 사이에 순환 참조가 있으며, 어떤 패키지가 어디까지 의존해도 되는지 규칙과 이를 강제하는 장치가 없다.
> 4. 서비스가 `api`의 DTO·Converter·Security 타입에 의존해서 유스케이스 계층을 `api`에서 분리하기 어렵다.

## 대안(Alternatives)

> **A. 현 구조 유지 + 패키지만 정리**
> - 비용이 가장 작지만 도메인과 기술 구현의 경계는 여전히 규칙으로 강제되지 않는다.
>
> **B. 수평 분할**: `domain` / `infrastructure` / `api`
> - 외부 기술 구현을 `infrastructure`로 격리하고 의존 방향을 모듈 경계로 강제한다. 유스케이스(`application`) 모듈은 만들지 않고 서비스는 `api`에 둔다.
>
> **C. 헥사고날 완전 분할**: `domain` / `application` / `infrastructure` / `api`
> - 가장 정석이지만 서비스가 api DTO·Converter·Security 타입에 의존하고 있어 선행 작업(Command/Result로 분리)이 크다. 지금 하면 "이동 + 설계 변경"이 섞인다.
>
> **D. 도메인별 수직 분할** (`record`, `user`, ... 각각 모듈)
> - 모듈 간 순환(예: record ↔ user)과 Q-type, Flyway 위치 문제가 크다. 도메인 경계가 안정된 뒤에 검토한다.

## 결정(Decision)

> **B를 채택한다.** 헥사고날(Ports & Adapters)을 단순화한 구조로, 도메인이 포트를 정의하고 아웃바운드 어댑터는 `infrastructure`가 구현하며, 유스케이스와 인바운드 어댑터는 `api`가 갖는다. 기능 단위로 패키지를 나눈다(package-by-feature).

### 1. 모듈과 책임

> | 모듈 | 책임 |
> |---|---|
> | `domain` | 엔티티, 값 객체, 도메인 규칙, 예외/에러코드, Repository **인터페이스**와 외부 기술 **포트**(`*Provider`), 캐시 키(`*RedisKey`)·Kafka 토픽 이름 상수, Flyway 마이그레이션 |
> | `infrastructure` | 포트의 구현: Repository 구현(JPA/QueryDSL), Redis·메일·비밀번호·S3·Kafka 토픽 설정 |
> | `api` | Controller, Service, Converter, Validator, DTO, Kafka Consumer·Relay 스케줄러, Security, SSE |
>
> - 웹 보안(필터, `TokenProvider`, 권한 애노테이션)은 애플리케이션을 호출하는 입구를 지키는 인바운드 어댑터이므로 `api`에 둔다. `infrastructure`는 애플리케이션이 호출하는 바깥 시스템의 구현만 둔다.

### 2. 의존 규칙

> ```
> api ──▶ domain ◀── infrastructure
> api ┄┄▶ infrastructure   (runtimeOnly)
> ```
>
> | from \ to | domain | infrastructure | api |
> |---|---|---|---|
> | domain | - | ✕ | ✕ |
> | infrastructure | ○ | - | ✕ |
> | api | ○ | `runtimeOnly` | - |
>
> - `api`는 `infrastructure`를 **`runtimeOnly`로만** 연결한다. 구현 클래스를 컴파일 시점에 볼 수 없으므로, 구현은 `domain`의 포트로만 쓴다. 경계가 규칙이 아니라 빌드로 강제된다.
> - 따라서 `api`가 컴파일 시점에 쓰는 것(포트, 캐시 키, Kafka 토픽 이름)은 `domain`에 둔다.
> - `domain`은 Spring Web(MVC), Kafka, Redis, 메일, AWS, HTTP 클라이언트 같은 구현 기술에 의존하지 않는다. JPA와 spring-data의 엔티티 지원은 허용한다.
> - 서비스가 다른 서비스의 구체 클래스를 직접 의존하지 않고 Repository/`XxxProvider` 인터페이스를 경유한다.
> - Gradle에서 `api(...)`(전이 노출)는 계약에 쓰이는 타입에만, 나머지는 `implementation(...)`을 쓴다. 의존은 모듈별 `build.gradle.kts`에 명시하고, 루트에서 일괄 주입하지 않는다.

### 3. 패키지 표준

> - **레이어 폴더는 파일 수와 관계없이 일관되게 유지한다.** 파일 수에 따라 폴더를 만들거나 합치면 파일이 하나 늘 때마다 구조와 import가 바뀌고 기능마다 모양이 달라진다. 단일 파일 패키지는 문제가 아니다. 문제는 순환, `common` 잡동사니, 잘못 놓인 파일이다.
> - **기능 패키지 안의 레이어 이름은 허용 목록으로 고정한다.** 목록 밖의 이름은 ArchUnit이 막는다.
>
>   | 이름 | 역할 |
>   |---|---|
>   | `controller` | 인바운드 웹 어댑터 |
>   | `service` | 유스케이스 |
>   | `dto` / `converter` | 요청·응답·메시지 페이로드 / 도메인 ↔ DTO 변환 |
>   | `validator` / `updater` | 파라미터·비즈니스 검증 / 변경 마스크 기반 엔티티 갱신 |
>   | `consumer` / `scheduler` | 인바운드 메시지·스케줄러 어댑터 |
>   | `provider` | 아웃바운드 포트. 인터페이스는 `domain`, 구현은 `infrastructure` |
>   | `config` | 해당 기능 전용 설정 |
>
> - **`common`은 레이어가 아니라 역할로 나눈다.** 횡단 관심사만 두며 기능 패키지에 의존하지 않는다. 허용하는 역할 이름은 ArchUnit이 고정한다.
> - **`domain`은 애그리거트 기준으로 나눈다.** 루트와 소속 하위 엔티티를 한 패키지 아래에 두고, 안쪽은 기존 `entity/repository/vo` 레이어를 유지한다.
>   - 애그리거트 밖에서는 루트 패키지로만 접근한다(ArchUnit). 애그리거트 사이의 참조는 ID 또는 읽기 전용 조회로 한다.
>   - 서브 엔티티의 팩토리는 `internal`로 제한하고 루트가 단일 진입점이다.
>   - 루트 ↔ 자식 패키지의 순환은 JPA 양방향 연관이 그대로 드러난 것이라 한 애그리거트 안에서는 정상이다. 합치지 않고 경계를 규칙으로 고정한다.
> - Outbox 엔티티를 담은 `sync` 패키지는 클래스명(`*SyncEvent`), `api`의 `sync`, 테이블명과 일관되므로 이름을 유지한다. 헥사고날 용어(adapter, port)로의 전면 개명은 `application` 모듈을 분리할 때 함께 판단한다.

### 4. 영속성과 스키마

> 1. **JPA 엔티티 = 도메인 엔티티**로 유지한다. 순수 도메인 모델 분리는 하지 않는다.
> 2. **`QueryFilter`는 필드만 가진 순수 데이터 클래스**로 둔다. 조건식과 정렬 번역은 `infrastructure`의 QueryDSL Repository가 맡는다. 엔티티가 `domain`에 있으므로 Q-type 생성(kapt)은 `domain`에 남지만, Q-type을 쓰는 코드는 `infrastructure`로 모이고 Repository 인터페이스는 QueryDSL에 묶이지 않는다.
> 3. **Flyway SQL은 `domain`에 둔다.** 컬럼 추가 시 마이그레이션과 엔티티 필드가 항상 같은 변경 단위라 같은 모듈에 있어야 리뷰와 롤백이 한 번에 된다.

### 5. 유스케이스 모듈(`application`)

> 이번 결정에서는 만들지 않는다. 서비스가 `api` DTO·Converter·Security 타입을 import하지 않는 상태(Command/Result 사용)가 선행 조건이며, 분리할지는 그 뒤에 별도로 판단한다.

### 6. 강제 장치

> 1. **Gradle 모듈 경계**: 위 표의 ✕와 `runtimeOnly`는 컴파일 에러로 막힌다.
> 2. **ArchUnit 테스트**(`DependencyRuleTest`): 모듈만으로 막지 못하는 규칙, 즉 패키지 순환 금지, `domain`의 구현 기술 의존 금지, 레이어·`common` 역할 이름 허용 목록, 애그리거트 경계, 포트·`*RedisKey`·`KafkaTopics`·`*RepositoryAdapter`의 위치를 코드로 고정한다. 기존 위반은 freeze 저장소에 두고 새 위반만 막는다.
> 3. 구조 변경은 "이동과 동작 변경을 섞지 않는다"를 원칙으로 한다. 이동 전에 현재 동작을 고정하는 테스트(컨텍스트 스모크, 계약 테스트)를 먼저 두고, 이동 후에 같은 테스트가 수정 없이 통과해야 한다.

## 결과(Consequences)

> **얻는 것**
> - 도메인 규칙과 외부 기술 구현의 경계가 모듈로 드러나고, 구현 교체와 테스트의 경계가 명확해진다.
> - 의존 규칙이 빌드와 테스트로 강제되어 위반이 리뷰에 드러난다.
> - `domain`이 구현 기술(QueryDSL, 웹, 메일 등)에서 벗어난다.
>
> **치르는 것 / 위험**
> - **Spring Security/`TokenProvider` 결합**: 서비스가 Security 타입을 직접 쓰는 곳은 `application` 분리 전까지 `api`에 남는다.
> - 모듈이 늘어 빌드 설정과 테스트 설정이 늘어난다. 테스트는 CI가 도는 `api`에 두고, 모듈 전용 테스트 소스는 필요할 때 만든다.
>
> **이 ADR이 다루지 않는 것**
> - 프로세스 분리(`worker`)와 멀티 인스턴스 확장. 현재 구조는 확장을 위한 구조가 아니다. Outbox relay 스케줄러가 `api` 안에 있고 relay가 단일 인스턴스를 가정하며, SSE Emitter 맵이 인스턴스 로컬이라는 제약이 남아 있다. 별도 ADR에서 결정한다.

## 결정 변경 이력

> - 2026-10-24: 캐시 키(`CacheKeyPrefix`)의 이동 위치를 `infrastructure`에서 `domain`의 기능별 `*RedisKey`로 수정했다. `api`가 `infrastructure`를 `runtimeOnly`로만 연결하면 `api`가 `infrastructure`의 키를 import할 수 없기 때문이다. 같은 이유로 Kafka 토픽 이름 상수도 `domain`에 둔다. 상세는 [infrastructure 모듈 설계](../docs/design/infrastructure-module.md).
> - 2026-10-09: Flyway SQL은 `domain`에 두는 것으로 확정했다. 초안의 모듈 표에는 `infrastructure`로 적혀 있었다.
> - 이 ADR의 초안은 `worker` 모듈 신설과 인스턴스 확장을 같은 목표로 담고 있었으나, 책임 분리와 확장은 다른 영역의 결정이라 이 ADR의 범위에서 분리했다.
