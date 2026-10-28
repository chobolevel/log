# CLAUDE.md

log-be — 기록(Record) 중심 개인 서비스의 백엔드. Kotlin 1.8.21 · Spring Boot 3.1.0 · JVM 17 · Gradle(Kotlin DSL) 멀티 모듈.

## 모듈 구조

- `domain` — 엔티티, Repository 인터페이스(포트), 외부 기술 포트(`*Provider`), Redis 키(`*RedisKey`)·Kafka 토픽 이름(`KafkaTopics`), QueryFilter, Flyway 마이그레이션
- `infrastructure` — 포트의 구현: Repository 구현체(JPA + QueryDSL), Redis 캐시·분산 락, 이메일, 비밀번호, S3, Kafka 토픽 빈
- `api` — Controller, Service, Converter, Validator, Updater, DTO, Kafka consumer·relay, Security
- 의존 방향은 `api → domain ← infrastructure`다. `api`는 `infrastructure`를 `runtimeOnly`로만 연결하므로 구현 클래스를 import할 수 없고, `domain`의 포트로만 쓴다.
- 새 외부 기술(캐시, 메일, 저장소 등)을 붙일 때는 `domain`에 포트를 먼저 정의하고 구현은 `infrastructure`에 둔다. 위치 규칙은 `DependencyRuleTest`가 검증한다.
- 패키지는 도메인 > 서브도메인 > 레이어 순서로 나누고, 레이어 폴더는 파일이 1개여도 만든다. 허용 레이어 이름, `common` 역할 목록, 순환 금지는 `DependencyRuleTest`(ArchUnit)가 검증하므로 여기에 다시 적지 않는다. 기존 위반은 `api/src/test/resources/archunit_store`에 고정되어 있고 새 위반은 테스트가 실패한다.
- 구조 재편의 배경과 계획은 `decisions/module-and-package-structure.md`를 따른다.

## 명령어

```bash
./gradlew :api:test                          # 전체 테스트 (Testcontainers 때문에 Docker 필요)
./gradlew :api:test --tests "<클래스 FQCN>"  # 특정 테스트
./gradlew ktlintCheck                        # lint 검사 (CI와 동일)
./gradlew ktlintFormat                       # 자동 포맷
```

## Git 커밋 규칙

- 커밋 메시지에 Claude, Co-Authored-By, Generated with Claude Code 등 AI 관련 내용을 포함하지 않는다.
- 커밋 메시지는 변경 내용을 간결하게 한국어로 작성한다.
- 형식: `<type>: <내용>` (예: `feat: 게시글 등록 API 추가`, `fix: 토큰 만료 예외 처리`)

### 커밋 진행 흐름

작업 완료 후 커밋은 아래 순서로 진행한다. **사용자 확인 전에 절대 커밋하지 않는다.**

1. `git add .` 로 변경 파일을 스테이징한다.
2. 변경 내용을 바탕으로 커밋 메시지 초안을 작성하여 사용자에게 제안한다.
3. 사용자가 확인(또는 메시지 수정)하면 그때 커밋을 실행한다.

## 핵심 규칙

1. **타입을 명시한다.** 클래스 프로퍼티, 컬렉션·제네릭, 함수 반환 타입은 선언한다. 생성 함수명이나 리터럴로 타입이 명백한 지역 변수, 람다 파라미터, for 루프 변수만 생략할 수 있다. 예시 코드를 작성할 때도 같다. (근거: IDE 없이 읽는 PR 리뷰에서 타입 파악이 어렵다 — *Effective Kotlin*)
2. **Repository는 인터페이스에만 의존한다.** 새 조회 메서드는 `XxxRepository` 인터페이스에 먼저 정의하고 `infrastructure`의 `XxxRepositoryAdapter`에서 구현한다. JPA/QueryDSL 세부 사항을 api 계층에 노출하지 않는다.
3. **도메인 간 의존.** `api/service`는 여러 도메인을 조율하므로 타 도메인 Repository를 직접 참조해도 된다. `domain` 내부에서 타 도메인 Repository를 참조하는 것은 금지한다.
4. **엔티티를 API 응답으로 직접 반환하지 않는다.** 양방향 연관관계가 Jackson 직렬화에서 무한 참조(StackOverflowError)를 일으킨다. 반드시 Converter로 DTO로 변환한 뒤 반환한다.
5. **검증은 3단으로 나눈다.** 컨트롤러의 `XxxParameterValidator`(요청 형식), 서비스의 `XxxBusinessValidator`(시스템 상태·정책), 엔티티 `require()`(불변식). 단일 필드의 정적 제약은 Bean Validation을 먼저 쓴다. 상세: `docs/conventions/validation-layering.md`
6. **테스트는 성공과 실패 케이스를 함께 작성한다.** 서비스 단위 테스트는 Kotest `BehaviorSpec` + MockK다. 상세: `docs/conventions/testing.md`

## 작업 원칙

- 큰 변경은 구현 전에 설계를 먼저 합의한다. 변경은 "동작 변화 하나" 단위로 나누고 한 PR에는 하나만 담는다.
- 구현 전에 검증 방법(테스트 케이스)을 정한다. 수동 API 확인만으로 완료로 보지 않는다.
- 위 규칙을 어기는 편이 낫다고 판단되면 조용히 우회하지 말고 이유와 함께 알린다.

## 참고 문서

- `docs/conventions/` — 검증 계층, 테스트 작성 규칙
- `decisions/` — 구조 수준 결정(ADR)
