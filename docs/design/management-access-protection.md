# 관리(actuator) 영역 접근 보호와 관리 포트 분리

- 상태: 초안
- 작성일: 2026-11-13
- 관련: [모듈·패키지·의존 구조 ADR](../../decisions/module-and-package-structure.md), 0단계 백지화 커밋 `ba08e22`(기존 `management:` 설정 제거 + `ActuatorExposureConfigTest`), 로깅 재설계의 선행 단계(이후: 로그 계약 → 메트릭 → 트레이스 → 알림)

## 배경과 목표

로깅 재설계의 메트릭 단계는 Prometheus가 앱의 `prometheus` 엔드포인트를 긁는 것이 출발점이다. 그전에 그 엔드포인트를 안전하게 노출할 구조를 먼저 만든다. 기존 설정은 `/monitors/**` 전체 공개, 노출 `*`(heapdump·loggers 포함)였고, 0단계에서 전부 지웠다. 지금은 `/actuator/health` 하나가 앱 포트(9565)에 무인증으로 열려 있고 프로브·토큰·포트 분리가 없다.

끝나면 다음이 가능해진다.
- 관리 엔드포인트는 클라이언트별 정적 Bearer 토큰 없이는 접근할 수 없다. 토큰이 없거나 잘못 설정돼도 열리지 않고(fail-closed) 앱은 정상 기동한다.
- 컨테이너 헬스체크용 프로브(liveness·readiness)는 토큰 없이 UP/DOWN만 돌려준다.
- 관리 엔드포인트는 별도 포트(9566)의 별도 톰캣 스레드 풀에서 돌고, 앱 포트에서는 존재하지 않는다(404).
- Prometheus가 토큰을 들고 `prometheus` 엔드포인트를 긁을 수 있다.

## 결정 사항

| 결정 | 선택 | 대안 | 선택 이유 |
|---|---|---|---|
| 인증 방식 | 정적 Bearer 토큰 | OAuth2 Client Credentials / mTLS / DB 기반 클라이언트 | 토큰 서버나 DB가 관측 경로의 의존이 되면 그 장애를 진단할 경로가 같이 죽고 오탐이 생긴다. mTLS는 서버가 나뉘는 시점의 확장 경로로 남긴다. OAuth2는 모니터링이 아니라 별도 학습 과제다 |
| 클라이언트별 토큰 | `management-access.clients.<이름>.tokens`(쉼표 구분 = 로테이션 시 이전·새 토큰 동시 허용). 클라이언트는 `application.yml`에 선언하고 토큰 값은 환경변수(`${PROMETHEUS_ACCESS_TOKENS:}`)로 주입한다. 현재 클라이언트는 `prometheus` 하나 | 환경변수만으로 클라이언트 추가 / 공용 토큰 하나 | 인증 주체가 클라이언트 이름이 되어 이후 클라이언트별 엔드포인트 권한을 추가 변경만으로 얹을 수 있다. 클라이언트 목록을 코드에 선언해 두면 PR에서 누가 접근하는지 보인다 |
| 설정 접두사와 이름 | `management-access` 접두사, `ManagementAccess*`·`ManagementClient` 이름 | `monitoring.*` | actuator의 `management.*`와 접두사가 겹치지 않고, 이름이 보호 대상(관리 영역)을 가리킨다 |
| 토큰 검증 규칙 | 32자 미만은 무효, 같은 토큰이 여러 클라이언트에 있으면 클라이언트 이름 오름차순에서 뒤쪽이 무효. 무효 사유는 ERROR 로그로 남기고 **토큰 값은 로그에 쓰지 않는다**. 유효한 토큰이 0개면 ERROR 한 번 | 약한 토큰 허용 / 중복 시 기동 실패 | 모니터링 설정 실수가 앱 가용성에 영향을 주면 안 된다(아래 fail-closed). 정렬 순서로 정하면 맵 바인딩 순서와 무관하게 결과가 결정적이다 |
| fail-closed | 유효한 토큰이 없으면 관리 엔드포인트는 전부 401, 프로브는 200, 앱은 정상 기동 | 기동 실패 / 인증 없이 열림 | 열리는 쪽 실패(fail-open)는 허용하지 않는다. 기동 실패는 관측 설정 실수가 사용자 서비스를 죽인다. 대가: 토큰 주입 실수 시 메트릭이 조용히 끊긴다 → ERROR 로그와, 이후 알림 단계의 `up` 감시가 보완한다 |
| 토큰 비교 | 제시 토큰과 등록 토큰을 각각 SHA-256 해시한 뒤 `MessageDigest.isEqual`로 비교하고, 일치 후보를 찾아도 전체 후보를 끝까지 비교한다 | `String.equals` / 해시 맵 조회 | 길이·접두사에 따른 응답 시간 차이를 없앤다. 해시 후 비교는 길이가 달라도 같은 비용이다 |
| 토큰 전달 | `Authorization: Bearer <token>` 헤더만 허용한다(스킴 대소문자 무시) | 쿼리 파라미터 / 커스텀 헤더 | 쿼리 파라미터는 액세스 로그·프록시 로그에 남는다. Prometheus `authorization` 설정이 이 형식을 그대로 지원한다 |
| 인증 필터 위치 | 관리 전용 `SecurityFilterChain`(`@Order(1)`, `EndpointRequest.toAnyEndpoint()`)에만 필터를 둔다. 기존 앱 체인(JWT, `GuestIdAssignFilter`)은 건드리지 않는다 | 기존 체인에 경로 분기 추가 | 앱 체인에 관리 로직이 섞이지 않고, 관리 응답에 비회원 쿠키(`GuestIdAssignFilter`)가 붙지 않는다. 관리 포트 분리 후에도 같은 체인이 자식 컨텍스트에 적용된다(S-2 테스트가 증명) |
| 인증 실패 응답 | 401, `WWW-Authenticate: Bearer`, 본문 없음. 실패 사유(없음·형식 오류·불일치)를 구분하지 않는다 | 403 / 사유별 메시지 | 사유를 구분해 알려 주면 토큰 탐색 단서가 된다. 인증 안 됨은 401이다 |
| 프로브 | `management.endpoint.health.probes.enabled=true`. `/actuator/health/liveness`·`/actuator/health/readiness` **정확한 두 경로만** 무인증 | `/actuator/health/**` 전체 무인증 / 프로브 사용 안 함 | 프로브는 UP/DOWN만 돌려주는 별도 그룹이라 안전하다. `/actuator/health/<컴포넌트>` 같은 하위 경로를 열어 두면 내부 상태가 노출된다. 프로브를 S-1에 넣어야 인증만 단독 머지해도 컨테이너 헬스체크가 안전하다 |
| 집계 헬스 | `/actuator/health`는 토큰 필요 | 무인증 | 집계 헬스는 DB·Redis·Kafka 연결을 실제로 확인한다. 무인증이면 누구나 호출할 때마다 외부 의존 확인을 일으키는 부하 증폭 경로가 된다 |
| 프로브 추가 경로 | `management.endpoint.health.probes.add-additional-paths`는 켜지 않는다 | `/livez`·`/readyz`를 앱 포트에 노출 | 이 옵션은 메인 포트에 프로브를 연다. 관리 포트 분리 원칙(앱 포트에는 관리 경로가 없다)과 충돌한다 |
| 노출 목록 | `health`, `prometheus` 두 개만. `ActuatorExposureConfigTest`가 이미 막는다 | `*` | `loggers`·`heapdump`·`threaddump`·`env`는 쓸 이유가 없고 노출 비용이 크다. 필요해지면 테스트의 허용 목록과 함께 이 문서를 먼저 고친다 |
| 관리 포트 | `management.server.port: 9566`, **`application.yml`(모든 프로파일)** | production 프로파일만 | 로컬·테스트·운영의 관리 체인 구조가 같다. 로컬에서 보안 체인과 포트 분리를 그대로 재현한다 |
| 바인딩 주소 | `management.server.address`는 지정하지 않는다. 호스트에 9566을 매핑하지 않는 것은 배포 설계의 몫이다 | `127.0.0.1` 고정 | 같은 compose 네트워크의 Prometheus가 접근해야 하므로 컨테이너 안에서는 모든 인터페이스에 바인딩해야 한다. 토큰이 있으므로 로컬 노트북에서 포트가 열려도 fail-closed다 |
| base-path | 기본 `/actuator` | `/monitors` 등 커스텀 | 포트 분리로 경로 은닉의 이득이 없다. 표준 경로가 Prometheus·K8s 문서와 일치한다 |
| 패키지 | `api/common/security`에 평평하게 둔다. 새 역할 패키지를 만들지 않는다 | `common/management` 신설 | `DependencyRuleTest`의 허용 역할 목록에 `security`가 이미 있고, 기존 보안 클래스가 같은 방식으로 평평하게 놓여 있다 |
| 이음매 | 토큰→클라이언트 이름 판별을 `ManagementClientAuthenticator` 인터페이스로 둔다. 구현은 정적 토큰 하나 | 필터가 설정을 직접 읽음 | 나중에 DB·IdP 기반 구현이나 비상용 정적 토큰 병행으로 교체할 이음매다. `domain` 포트로 올리지 않는다(관리 영역 인증은 `api` 안의 보안 관심사이고 구현 기술 교체가 아님) |
| 인증 실패 관측 | 이번 범위에서 제외 | 401 WARN 로그 / 실패 카운터 | 요청 단위 401 로깅은 로그 계약 단계의 4xx 정책과 함께, 메트릭은 메트릭 단계에서 정한다. 이번에는 기동 시 설정 오류 ERROR 로그만 넣는다 |
| 배포 시점 | S-1과 S-2는 각각 머지하되 **배포는 함께** | S-1만 먼저 배포 | 0단계로 이미 기존 `/monitors/prometheus` 수집이 끊겨 있다. S-2가 나가야 수집이 복구된다 |

## 영향 범위

코드를 읽어서 확인한 현재 상태:
- `WebConfiguration`의 앱 체인은 `anyRequest().permitAll()`이다. 관리 엔드포인트용 체인은 없다.
- `application.yml`·`application-production.yml`에 `management:` 블록이 없다(0단계). `api/build.gradle.kts`에 `starter-actuator`와 `micrometer-registry-prometheus`가 있다.
- `ActuatorExposureConfigTest`가 yml 수준에서 노출 목록을 `health`, `prometheus`로 제한한다.
- `@ConfigurationPropertiesScan("com.chobolevel.api.common")`가 설정 클래스를 자동 등록한다.
- `ApplicationContextSmokeTest`는 Testcontainers(MySQL·Redis)로 전체 컨텍스트를 띄우며 `MockMvc`를 쓴다. `MockMvc`는 두 번째 톰캣(관리 포트)을 띄우지 않는다.

**추가 (S-1)** — `api/common/security`
- `ManagementAccessProperties(clients: Map<String, Client>)`, `Client(tokens: List<String>)`
- `ManagementClientAuthenticator`(인터페이스, `authenticate(token): String?` = 클라이언트 이름 또는 null)와 정적 토큰 구현(검증·해시·상수 시간 비교, 설정 오류 ERROR 로그)
- `ManagementTokenAuthenticationFilter`, `ManagementAuthenticationEntryPoint`
- `ManagementSecurityConfiguration`: 관리 전용 `SecurityFilterChain`(`@Order(1)`). 프로브 두 경로 `permitAll`, 나머지 관리 경로 `authenticated`. 기존 `WebConfiguration`은 수정하지 않는다
- `application.yml`: `management.endpoint.health.probes.enabled`, `management-access.clients.prometheus.tokens`
- 테스트 인프라: DB·Kafka·Redis·Redisson·Flyway 자동 구성과 `DomainConfigurationLoader`를 제외한 최소 `@SpringBootTest` 구성(`@AutoConfigureObservability` 포함). 이후 슬라이스가 재사용한다

**변경 (S-2)**
- `application.yml`: `management.server.port: 9566`, `management.endpoints.web.exposure.include: health,prometheus`
- 관리 체인이 자식(관리) 컨텍스트에 적용된다는 가정을 테스트로 고정한다

**추가 (S-3)**
- 운영 문서(위치는 S-3에서 결정: `docs/` 아래 한 파일)

**바뀌는 외부 계약**
- 앱 포트(9565): `/actuator/**`가 S-2 이후 404. (S-1 단계에서는 `/actuator/health`가 401로 바뀐다)
- 관리 포트(9566): 신규. `Authorization: Bearer` 필요(프로브 제외)
- 환경변수: `PROMETHEUS_ACCESS_TOKENS`(신규, 쉼표 구분)
- DB 스키마·이벤트 페이로드·캐시 키 변경 없음

## 슬라이스

| # | 동작 변화 | 영향 모듈/패키지 | 위험도 |
|---|---|---|---|
| S-1 | 관리 엔드포인트가 클라이언트별 Bearer 토큰 없이는 401이 된다. 프로브 두 경로만 무인증. 토큰이 없거나 무효면 관리 엔드포인트는 전부 401이고 앱은 기동한다. 아직 앱 포트에서 동작한다 | `api/common/security`, `api` 설정·테스트 | 높음 |
| S-2 | 관리 엔드포인트가 별도 포트(9566)로 이동하고 `prometheus`를 노출한다. 앱 포트에서는 `/actuator/**`가 404다 | `api` 설정·테스트 | 높음 |
| S-3 | 운영 문서: Prometheus 스크레이프 설정 예시, 토큰 생성·로테이션 절차, 관리 포트 비노출 원칙, 로컬에서 토큰 주입 방법 | `docs/` | 낮음 |

S-2는 S-1이 머지된 뒤에만 가능하다. S-1만 머지된 상태도 안전하다(인증이 있고 프로브가 열려 있다). S-2를 롤백하면 S-1의 보호가 남는다.

## 슬라이스별 테스트 케이스

위험도 기준은 [README](README.md)를 따른다. 테스트는 먼저 작성해 실패를 확인한 뒤 구현한다.

### S-1 클라이언트별 Bearer 토큰 인증 + 프로브 무인증 + fail-closed

- 단위 (`ManagementClientAuthenticator` 구현):
  - 등록된 유효 토큰이면 클라이언트 이름을 돌려준다
  - 등록되지 않은 토큰, 빈 문자열이면 null
  - 클라이언트에 토큰이 둘(쉼표 구분 로테이션)이면 둘 다 같은 클라이언트로 인증된다
  - 두 클라이언트가 각자 다른 토큰을 가지면 각자 자기 이름으로 인증된다
  - 32자 미만 토큰은 무효(인증 안 됨)이고 ERROR 로그가 남는다. **로그 본문에 토큰 값이 없다**
  - 같은 토큰이 두 클라이언트에 있으면 이름 오름차순에서 뒤쪽 클라이언트로는 인증되지 않고 ERROR 로그가 남는다
  - 유효 토큰이 0개(클라이언트 없음, 토큰 빈 문자열, 전부 무효)면 어떤 입력도 null이고 ERROR 로그가 한 번 남는다
  - 쉼표 구분 값 앞뒤 공백과 빈 항목(`a,,b`)은 무시한다
- 단위 (`ManagementTokenAuthenticationFilter`):
  - `Authorization: Bearer <유효>`이면 인증 객체(주체 = 클라이언트 이름)를 SecurityContext에 넣고 체인을 계속 진행한다
  - 헤더 없음, `Basic ...`, `Bearer`만 있고 토큰 없음, 토큰 뒤에 공백·추가 값, 무효 토큰이면 인증 객체를 넣지 않는다
  - 스킴 대소문자(`bearer`, `BEARER`)는 허용한다
  - 쿼리 파라미터(`?access_token=`)의 토큰은 무시한다
  - authenticator가 예외를 던져도 인증 객체를 넣지 않는다(fail-closed)
- API / 통합 시나리오 (최소 `@SpringBootTest` + MockMvc, 같은 포트):
  - 유효 토큰으로 `GET /actuator/health` → 200
  - 토큰 없음·무효·형식 오류 → 401, `WWW-Authenticate: Bearer`, 본문 없음, 세 경우의 응답이 같다
  - 로테이션: 한 클라이언트에 토큰 두 개를 설정하면 두 토큰 모두 200
  - `GET /actuator/health/liveness`, `/actuator/health/readiness` → 토큰 없이 200, 본문은 `{"status":"UP"}`뿐(컴포넌트 상세 없음)
  - `GET /actuator/health/db`, `/actuator/health/livenessState` 등 프로브 정확 경로가 아닌 하위 경로 → 토큰 없이 401
  - 토큰이 하나도 설정되지 않은 컨텍스트에서 앱이 기동하고, `/actuator/health` → 401, 프로브 → 200
  - 보호 대상과 무관한 경로: 노출되지 않은 `/actuator/env` → 404(체인이 매칭하지 않으므로 앱 체인이 처리), 앱 API 경로(`/api/v1/...`)는 토큰 없이 기존대로 동작한다(앱 체인 불변)
  - 관리 엔드포인트 응답에 `Set-Cookie`(비회원 식별자)가 없다
  - 외부 의존이 DOWN인 `HealthIndicator`를 하나 등록하면: 집계 `health`는 토큰 있을 때 503, 프로브 liveness는 토큰 없이 200
- 경계값 / 예외:
  - 토큰 길이 31자는 무효, 32자는 유효
  - 토큰이 같은 클라이언트 안에서 중복 나열돼도 오류 없이 하나로 취급한다
  - `ActuatorExposureConfigTest`가 계속 통과한다(S-1에서 노출 목록을 넓히지 않는다)
- 변이 검증(구현 후 수동으로 코드를 깨뜨려 위 테스트가 실패하는지 확인, 결과를 PR에 기록): 필터 제거 / 비교를 항상 true로 / 예외 시 인증 통과(fail-open) / 32자 미만 허용 / 프로브 매처를 `/actuator/health/**`로 확대 / `@Order(1)` 제거

### S-2 관리 포트 분리 + prometheus 노출

- 단위: 없음(설정 변경). `ActuatorExposureConfigTest`를 갱신해 `management.server.port`가 설정돼 있고 노출 목록이 정확히 `health`, `prometheus`임을 확인한다
- API / 통합 시나리오 (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `management.server.port=0`, 실제 HTTP 클라이언트로 두 포트를 호출. `MockMvc`는 관리 포트를 띄우지 않으므로 쓰지 않는다):
  - 관리 포트에서 토큰 없이 `/actuator/prometheus` → 401. **S-1의 체인이 관리(자식) 컨텍스트에도 적용된다는 것의 증명이다. 이 테스트가 200을 받으면 체인이 적용되지 않은 것이므로 반드시 먼저 실패를 확인한다**
  - 관리 포트에서 유효 토큰으로 `/actuator/prometheus` → 200, 본문에 Prometheus 텍스트 형식과 `jvm_memory_used_bytes`가 있다
  - 관리 포트에서 토큰 없이 프로브(liveness·readiness) → 200, 집계 `health`·`health/db` → 401
  - 앱 포트에서 `/actuator/health`, `/actuator/health/liveness`, `/actuator/prometheus` → 토큰 유무와 무관하게 404
  - 앱 포트의 기존 API는 영향 없이 동작한다
  - 관리 포트에서 `/actuator/env`, `/actuator/loggers`, `/actuator/heapdump` → 404(노출되지 않음, 토큰이 있어도 404)
  - 토큰 미설정 컨텍스트: 관리 포트의 `prometheus` → 401, 프로브 → 200
  - 관리 포트 요청이 앱 포트의 서블릿 스레드 풀과 분리되어 있다는 것은 설정(별도 포트)이 보장하므로 별도 테스트를 두지 않는다
- 경계값 / 예외:
  - 앱 포트에서 관리 경로가 404일 때 응답이 앱의 `ExceptionHandler`가 아닌 기본 404 처리로 나가도 본문에 내부 정보가 없다
- 변이 검증: 관리 포트 설정 제거 시 앱 포트 404 테스트가 실패 / 관리 체인의 `@Order(1)` 제거 시 관리 포트 401 테스트가 실패 / 노출 목록에 `env` 추가 시 `ActuatorExposureConfigTest`가 실패

### S-3 운영 문서

- 단위: 없음
- API / 통합 시나리오: 없음. 문서의 Prometheus 스크레이프 예시(`authorization` 설정)가 S-2 통합 테스트가 증명하는 형식(`Authorization: Bearer`)과 일치하는지 리뷰에서 대조한다
- 경계값 / 예외: 문서에 실제 토큰 값을 쓰지 않는다. 예시는 자리표시자만 쓴다

## 위험도와 리뷰 깊이

| # | 위험도 | 근거 | 정독 지점 |
|---|---|---|---|
| S-1 | 높음 | 인증·권한(Spring Security 체인)이고 열리는 쪽 실패가 곧 정보 노출이다 | ① 정적 토큰 authenticator의 검증·해시·상수 시간 비교(후보를 끝까지 비교하는지, 무효 토큰이 후보에서 빠지는지, 로그에 토큰 값이 없는지) ② 필터의 모든 실패 경로가 인증 객체를 넣지 않는지(예외 포함) ③ 체인의 매처 순서(프로브 두 경로 정확 일치 `permitAll` → 나머지 관리 경로 `authenticated`)와 `@Order(1)` ④ 기존 `WebConfiguration`이 수정되지 않았는지 ⑤ 변이 검증 결과 6종 |
| S-2 | 높음 | 인증이 적용되는 포트·노출 범위가 바뀌고, 체인이 관리 컨텍스트에 적용되지 않으면 전부 무인증으로 열린다 | ① `application.yml`의 포트·노출 목록 ② "관리 포트 + 토큰 없음 → 401" 테스트가 실제 HTTP로 관리 포트를 치는지(MockMvc로 대체되지 않았는지) ③ 앱 포트 404 테스트 ④ 변이 검증 결과 |
| S-3 | 낮음 | 문서뿐이다 | 토큰 값이 들어가지 않았는지, 스크레이프 예시가 S-2 계약과 맞는지 |

## 끝단 검증

S-2에 `ManagementEndToEndTest`(`@SpringBootTest(RANDOM_PORT)` + `ApplicationContextSmokeTest`와 같은 Testcontainers 구성, 최소 컨텍스트가 아닌 **실제 전체 컨텍스트**)를 둔다. 전체 컨텍스트에서 아래가 자동으로 증명된다.

1. 관리 포트: 토큰 없음 → `prometheus` 401 / 프로브 200
2. 관리 포트: 유효 토큰 → `prometheus` 200, `jvm_memory_used_bytes`와 `http_server_requests` 계열 메트릭 포함
3. 앱 포트: `/actuator/**` 404
4. 토큰 환경변수를 설정하지 않은 전체 컨텍스트도 기동되고 앱 API가 정상 동작한다
5. 실제 앱 체인(JWT·비회원 필터)이 관리 응답에 영향을 주지 않는다

수동 `curl` 확인은 완료 조건에 넣지 않는다.

## 비범위

- 인증 실패(401) 요청 단위 로깅·카운터: 로그 계약(4xx 정책)·메트릭 단계에서 정한다
- 클라이언트별 엔드포인트 권한: 지금은 모든 유효 클라이언트가 노출된 엔드포인트 전부를 쓸 수 있다. 이음매는 남겨 둔다
- 클라이언트 도메인(DB 기반 등록·폐기·키 로테이션·스코프·사용 이력)과 OAuth2 Client Credentials: 별도 후속 과제이며 관리 영역 인증의 기반으로 쓰지 않는다
- mTLS, IP 허용 목록
- `management.server.address` 바인딩 제한, 관리 포트의 호스트 매핑 제외(compose·배포 재설계의 몫)
- Prometheus·Grafana 서버 설정과 compose 파일(배포·관측 스택 단계)
- 메트릭 정책: 공통 태그(`application`, `env`), URI별 메트릭·히스토그램 설정은 메트릭 단계
- `show_sql`의 System.out 출력 정리와 500 응답의 `e.message` 노출은 로그 계약 단계에서 처리한다
- 앱 포트 API 인증 구조 변경

## 열린 질문

없음. 구현 중 확인할 기술 사항은 테스트가 증명한다: Spring Boot 3.1.0에서 `EndpointRequest` 매처가 프로브 하위 경로를 어떻게 매칭하는지(S-1), 부모 컨텍스트의 `@Order(1)` 체인이 관리 컨텍스트에 적용되는지(S-2). 둘 다 가정이 틀리면 테스트가 실패하므로 구현 전에 테스트를 먼저 쓰고 실패를 확인한다.
