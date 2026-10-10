# ADR: 모듈·패키지·의존 구조 재편

> 상태: **Draft** (미결정 사항은 맨 아래 "열린 결정" 참고. 확정 전에는 파일 이동을 시작하지 않는다.)

## 상황(Context)

> 현재 모듈은 `domain`, `api` 두 개다.
> - `domain`: 엔티티, Repository 인터페이스/구현체(JPA + QueryDSL), QueryFilter, 예외/에러코드, Flyway 마이그레이션
> - `api`: Controller, Service, Converter, Validator, Updater, DTO + Kafka 메시지/Consumer/Relay 스케줄러, Redis/Provider 구현, Security, SSE
>
> 목표는 **인스턴스가 늘어나는 환경(트래픽, 배포 분리)에서도 그대로 쓸 수 있는 구조**다. 배포는 비용상 한 서버의 Docker가 되더라도, 구조는 대규모 환경을 전제로 한다.

### 현황 조사 결과

> `api` 모듈 203개 파일 / 108개 패키지.
>
> | 종류 | 파일 수 |
> |---|---|
> | 요청/응답/기타 DTO | 62 |
> | Controller / Service / Converter | 20 / 20 / 14 |
> | Parameter·Business Validator | 9 / 7 |
> | Security / Provider / Config / Properties | 8 / 8 / 6 / 6 |
> | Scheduler / Consumer / Kafka 메시지 | 7 / 4 / 4 |
> | Updater / Facade | 6 / 1 |
>
> **모듈 문제**
> 1. `api`가 "요청/응답 처리"와 "백그라운드 작업(Outbox relay 스케줄러 4곳, 상태 동기화 Consumer 3곳)"을 한 프로세스에 함께 담는다. api 인스턴스를 늘리면 relay도 같이 늘어 중복 실행 위험이 생기고, 반대로 relay만 따로 늘리거나 끌 수 없다.
> 2. 외부 시스템 연동(Kafka, Redis, 메일, S3 등)의 구현이 `api`의 `common/provider`, `common/config` 등에 섞여 있다.
> 3. `domain`의 `build.gradle.kts`가 `starter-web`, `spring-web`(`api` 구성), `starter-mail`(미사용), `okhttp` 등 도메인과 무관한 의존을 가진다.
> 4. 루트 `subprojects {}`가 모든 모듈에 redis, jackson, coroutines, spring-cloud-aws를 일괄 주입한다. 모듈이 실제로 무엇에 의존하는지 빌드 파일에서 읽을 수 없다.
> 5. `domain`의 `QueryFilter`가 `QUser` 같은 Q-type과 `BooleanExpression`을 직접 사용하고, Repository 인터페이스가 이 `QueryFilter`를 받는다. 즉 인터페이스까지 QueryDSL 생성 코드에 묶여 있다.
>
> **패키지 문제**
> 1. `api`에 파일 1개짜리 패키지가 74개 — 레이어 이름(`controller/service/dto/...`)을 기계적으로 쪼갠 결과다.
> 2. `common`이 잡동사니 통이다 (`CacheKeyPrefix`가 모든 도메인의 키를 보유, 기능 Controller, `RecordTagPolicy` 같은 도메인 규칙).
> 3. 서브도메인(`follow`/`image`/`emotion`/`review`/`tag`/`vo`) 사이에 패키지 순환 참조가 있다. 애그리거트 단위로 묶이지 않았다.
> 4. `sync` 패키지는 이름과 달리 Outbox 엔티티를 담고 있어 의도를 읽기 어렵다.
> 5. 서비스 21개 중 16개가 `api`의 DTO를 import하고, Converter·Security 타입도 직접 쓴다 → 유스케이스 계층을 api에서 분리하기 어렵다.
> 6. 의존 규칙을 강제하는 장치가 없어 위 문제는 계속 늘어난다.

## 문제(Problem)

> 1. 프로세스(api / worker)의 책임과 확장 단위가 코드 구조에 드러나지 않는다.
> 2. 어떤 패키지가 어디까지 의존해도 되는지 규칙이 없고, 위반을 잡아낼 장치도 없다.
> 3. 구조를 한꺼번에 바꾸면 리뷰 불가능한 변경이 된다. 이동과 동작 변경을 섞지 않고 단계적으로 가야 한다.

## 대안(Alternatives)

> **A. 현 구조 유지 + 패키지만 정리**
> - 비용이 가장 작지만 api/worker 분리 불가. 확장 단위 문제가 남는다.
>
> **B. 수평 분할 + worker (선택 방향 C')**: `domain` / `infrastructure` / `api` / `worker`
> - 프로세스 분리와 외부 연동 격리를 얻는다. `application`(유스케이스) 모듈은 만들지 않고 서비스는 일단 `api`에 둔다.
>
> **C. 헥사고날 완전 분할**: `domain` / `application` / `infrastructure` / `api` / `worker`
> - 가장 정석이지만 서비스가 api DTO·Converter·Security 타입에 의존하고 있어 선행 작업(Command/Result로 분리)이 크다. 지금 하면 "이동 + 설계 변경"이 섞인다.
>
> **D. 도메인별 수직 분할** (`record`, `user`, ... 각각 모듈)
> - 모듈 간 순환(예: record ↔ user)과 Q-type, Flyway 위치 문제가 크다. 도메인 경계가 안정된 뒤에 검토한다. **보류**.

## 결정(Decision) — 제안

### 1. 모듈과 책임

> | 모듈 | 책임 | 실행 |
> |---|---|---|
> | `domain` | 엔티티, 값 객체, 도메인 규칙, Repository **인터페이스**, 도메인 예외/에러코드 | 라이브러리 |
> | `infrastructure` | Repository 구현(JPA/QueryDSL), Kafka·Redis·메일·S3 등 외부 연동 구현, Outbox 저장소, Flyway 마이그레이션 | 라이브러리 |
> | `api` | Controller, Service, Converter, Validator, DTO, Security, SSE(Emitter 레지스트리·dispatch Consumer·heartbeat) | 실행(웹) |
> | `worker` | Outbox relay 스케줄러, 상태 동기화 Consumer(좋아요/조회수/팔로우 read-repair), DLQ 재처리, 향후 배치 | 실행(비웹) |
>
> - SSE는 인스턴스 로컬의 Emitter 맵을 쓰므로 반드시 `api`에 남긴다 (알림 설계의 "로컬맵 → Redis 점진 전환"과 일치).
> - `application` 모듈은 **게이트 조건**을 만족할 때만 만든다: 서비스가 api DTO/Converter/Security 타입을 import하지 않는다(Command/Result 사용, 이미 `domain/subject/dto/*Command` 선례가 있음).

### 2. 의존 규칙

> ```
> api ──┐
>       ├──▶ infrastructure ──▶ domain
> worker┘
> api ──▶ domain, worker ──▶ domain
> api ✕ worker, worker ✕ api, domain ✕ (infrastructure/api/worker)
> ```
>
> | from \ to | domain | infrastructure | api | worker |
> |---|---|---|---|---|
> | domain | - | ✕ | ✕ | ✕ |
> | infrastructure | ○ | - | ✕ | ✕ |
> | api | ○ | ○ | - | ✕ |
> | worker | ○ | ○ | ✕ | - |
>
> - `domain`은 JPA 애노테이션과 spring-data 정도만 허용하고, web/mail/okhttp/kafka/redis는 금지한다.
> - Gradle에서 `api(...)`(전이 노출)는 계약에 쓰이는 타입에만, 나머지는 `implementation(...)`을 쓴다.
> - 루트 `subprojects {}`의 일괄 의존은 걷어내고 모듈별 `build.gradle.kts`에 명시한다.
> - 서비스가 다른 서비스를 직접 의존하지 않고 Repository/`XxxProvider` 인터페이스를 경유한다(기존 원칙 유지). Provider 인터페이스는 `domain`(또는 api가 소유하는 포트), 구현은 `infrastructure`.

### 3. 패키지 표준

> **domain / infrastructure: 애그리거트 기준**
> ```
> domain/record/            ← 애그리거트 루트와 소속 하위 엔티티를 한 패키지 아래에
>   Record.kt, RecordRepository.kt, RecordQueryFilter.kt, RecordTagPolicy.kt ...
>   like/, view/, emotion/, review/ ...   ← 같은 애그리거트에 속하는 것만 하위에
> ```
> - 현재의 `entity/repository/vo` 레이어 폴더는 애그리거트 안에서 파일이 적으면 만들지 않는다 (파일 3개 이하이면 평평하게).
> - 애그리거트 간 참조는 ID 또는 읽기 전용 조회로. 서브 엔티티의 팩토리는 `internal`, 루트가 단일 진입점.
> - 순환 의존(`follow`/`image`/`emotion`/`review`/`tag`/`vo`)은 소속 애그리거트로 흡수해 제거한다.
>
> **api: 기능(유스케이스) 기준**
> ```
> api/record/   RecordController, RecordService, RecordConverter, RecordRequest/Response ...
> api/record/like/...
> ```
> - 파일 1~2개짜리 레이어 폴더(`dto/` 등)는 만들지 않고 기능 패키지에 둔다. 파일이 많아질 때(대략 7개 이상)만 레이어 하위 폴더를 둔다.
> - `common`은 **횡단 관심사만** (예외 핸들러, 응답 래퍼, 보안 필터 등). 도메인 규칙·도메인별 상수·기능 Controller는 해당 기능/도메인으로 옮긴다.
>   - `CacheKeyPrefix` → 각 기능이 자기 키를 소유 (또는 infrastructure의 캐시 키 정의)
>   - `RecordTagPolicy` → `domain/record`
> - `sync` 패키지는 Outbox 엔티티의 실제 의미(`outbox`)로 이름을 바꾼다.

### 4. 강제 장치

> 1. **Gradle 모듈 경계** — 위 표의 ✕는 컴파일 에러로 막힌다.
> 2. **ArchUnit 테스트** — 모듈 안에서 막지 못하는 규칙: 패키지 순환 금지, `domain`에서 Spring web/kafka/redis import 금지, Controller→Repository 직접 접근 금지, `common`이 기능 패키지를 참조 금지.
> 3. 기존 `ActuatorExposureConfigTest` 처럼 "규칙을 테스트로 고정 → 위반이 리뷰에 드러남" 방식을 따른다.

### 5. 마이그레이션 단계 (모든 단계: 이동만, 동작 변경 없음, 테스트 전체 통과, 커밋 전 사용자 확인)

> | 단계 | 내용 | 위험 | 상태 |
> |---|---|---|---|
> | 0 | ArchUnit 의존 규칙 테스트 추가, 현재 위반은 허용 목록(freeze)으로 고정 | 낮음 | 완료 (`ae6f255`) |
> | 1-a | `domain`의 미사용 `RestTemplateConfiguration`과 web/mail/okhttp 의존 제거 | 낮음 | 완료 (`e5d11f9`) |
> | 1-b | 루트 `subprojects` 일괄 의존(redis, jackson, aws)을 모듈별로 분산, 미사용 coroutines 제거 | 낮음 | 완료 (`faa7839`) |
> | 1-c | 버전 카탈로그(`gradle/libs.versions.toml`) 도입, `resend-java` 버전 고정 | 낮음 | 완료 (`2a0a2d8`) |
> | 1-d | Jib용 `ext` 설정을 `api` 모듈로 이동 | 낮음 | 완료 (`56b901d`) |
> | 2 | 비활성 코드 제거 (`ChunkBatchScheduler`, `TaskletBatchScheduler`, 미사용 batch SQL) | 낮음 | 예정 |
> | 3 | 패키지 정리: `api` 단일 파일 패키지 평탄화, `common` 해체 | 중간 (import 대량 변경) | 예정 |
> | 4 | 패키지 정리: `domain` 애그리거트 기준 재배치, 순환 제거. 시작 전 하위 패키지 단위 순환 규칙을 ArchUnit에 추가해 현황을 고정 | 중간 | 예정 |
> | 4.5 | `QueryFilter` 7개를 순수 데이터 클래스로 변환(조건식 변환은 Repository 쪽으로) | 중간 | 예정 |
> | 5 | `infrastructure` 모듈 신설: Repository 구현·Provider 구현·Kafka/Redis/S3 설정 이동, `CLAUDE.md` 규칙 수정. Flyway SQL은 `domain`에 유지 | 높음 (Q-type, 자동 구성) | 예정 |
> | 6 | `worker` 모듈 신설: relay 스케줄러·상태 동기화 Consumer 이동, 별도 Jib 이미지, `worker`는 `spring.flyway.enabled=false` | 높음 | 예정 |
> | 7 | (게이트 통과 시) `application` 모듈 | 매우 높음 | 보류 |
>
> **1단계 결과**: 테스트 415개 통과. 변경 전후 `:api` 의존성 classpath diff로 의도한 변경만 있음을 확인했다(`okhttp:4.11.0`, `jakarta.mail`, coroutines 계열이 빠졌고 Jib 설정값은 동일).
>
> **이번 재편 범위 밖 후속 과제**
> - Jib JVM 플래그의 `-Djasypt.encryptor.password` 평문 값: 시크릿 로테이션 항목으로 별도 처리
> - worker 다중 인스턴스 시 relay 중복 실행(분산락 또는 `SKIP LOCKED`)
> - 마이그레이션 job 분리(배포 재설계)

## 결과(Consequences)

> **얻는 것**
> - api와 worker를 독립적으로 배포·확장할 수 있다. relay 중복 실행 이슈를 worker 쪽 락/리더 설정으로 한곳에서 다룬다.
> - 외부 연동 교체/테스트 경계가 명확해진다.
> - 의존 규칙이 코드로 강제된다.
>
> **치르는 것 / 위험**
> - **Q-type(QueryDSL)**: 엔티티가 `domain`에 있는 한 kapt 생성도 `domain`에 남는다. Repository **구현**을 `infrastructure`로 옮기면 Q-type을 `domain`의 `api` 구성으로 노출해야 한다. QueryFilter가 Q-type을 쓰는 한 `domain`↔QueryDSL 결합은 유지된다(허용 결정 필요).
> - **자동 구성**: `DomainConfigurationLoader`(AutoConfiguration.imports)가 이동 대상이므로, 미니 앱 테스트의 exclude 설정도 함께 바꿔야 한다.
> - **worker 확장 시 relay 중복 실행**: 현재 relay는 단일 인스턴스를 가정한다. worker를 2개 이상 띄우기 전에 락(분산락) 또는 `SKIP LOCKED` 청크 선점이 필요하다 — 이번 재편 범위 밖, 별도 작업.
> - **Spring Security/TokenProvider 결합**: 서비스가 Security 타입을 직접 쓰는 곳은 `application` 분리 전까지 `api`에 남는다.
> - 모듈이 늘어 빌드·Jib 설정과 테스트 설정이 늘어난다.
> - 6~7단계는 한 번에 끝낼 수 없고, 도메인별로 나눠 옮겨야 한다.

## 확정·미확정 결정

> **확정 (2026-10-09)**
> 1. JPA 엔티티 = 도메인 엔티티를 유지한다 (순수 도메인 모델 분리는 하지 않는다).
> 2. `CLAUDE.md`의 "Repository 구현체는 domain에 둔다" 규칙은 `infrastructure` 이동(5단계) 커밋에서 함께 고친다. 이동 전에 고치면 규칙이 현실과 어긋난다.
> 3. C' 채택: `application` 모듈은 서비스의 api DTO 의존이 해소된 뒤로 미룬다.
> 4. 모듈 이름은 `infrastructure`.
>
> 5. **Flyway SQL은 `domain`에 둔다. (확정)** 컬럼 추가 시 마이그레이션 SQL과 엔티티 필드가 항상 같은 변경 단위이므로 같은 모듈에 있어야 리뷰와 롤백이 한 번에 된다.
>    - 단, 실행 주체를 정한다: 마이그레이션은 `api`만 실행하고 `worker`는 `spring.flyway.enabled=false`. (Flyway 락이 동시 실행은 막지만, 어느 프로세스가 스키마를 바꾸는지 모호해지는 것을 피한다.) 또는 worker는 `validate`만. 배포 단계에서 마이그레이션 job을 분리하는 것은 배포 재설계 때 다룬다.
>
> 6. **(확정)** 아래 내용대로 진행한다.
> 6. **QueryFilter는 순수 데이터 클래스로 만들고, 조건 변환(`toPredicates`)은 `infrastructure`의 QueryDSL Repository로 옮긴다.**
>    - 현재: `domain`의 QueryFilter가 `QUser` 등 Q-type과 `BooleanExpression`을 직접 사용 → Repository 인터페이스(공개 계약)가 QueryDSL에 묶임.
>    - 변경: QueryFilter는 필드만 가진다(QueryDSL import 없음). 각 `XxxQuerydslRepository`(또는 같은 위치의 `XxxPredicates` 매퍼)가 필터를 조건식으로 바꾼다.
>    - 범위: QueryFilter 7개와 사용처 7곳. 기계적 변경이고, 기존 Repository 테스트(H2/MySQL 컨테이너)가 동작 보존을 검증한다.
>    - 엔티티가 JPA 엔티티로 `domain`에 남으므로 Q-type 생성(kapt)은 `domain`에 남는다. 달라지는 것은 "Q-type을 쓰는 코드"가 `infrastructure`로 모인다는 점이다.
>    - 대안(현행 유지)도 동작에는 문제 없다. 다만 5단계에서 Repository 구현을 옮길 때 `domain`이 QueryDSL을 `api` 구성으로 계속 노출해야 해서 경계가 흐려진다.
