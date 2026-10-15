# 검증 계층 분리 (Validation Layering)

검증은 책임에 따라 3단으로 나눈다.

| 레이어 | 호출 위치 | 의존성 | 검증 대상 |
|---|---|---|---|
| `XxxParameterValidator` | 컨트롤러 | 없음 (순수 함수) | 요청이 형식적으로 온전한가 — null, 길이, 정규식, updateMask별 필수값 |
| `XxxBusinessValidator` | 서비스 | Repository/Provider 등 | 현재 시스템 상태·정책상 이 동작이 허용되는가 — 중복 여부, 소유권, 정책 위반 |
| 엔티티 `require()` | 엔티티 생성자/팩토리 | 없음 | 이 엔티티가 어떤 경로로 만들어지든 항상 참이어야 하는 값 제약(불변식) |

**판별 기준**: 앞의 두 검증을 다 걷어내도 여전히 성립해야 하는 규칙이면 불변식(엔티티), DB 조회나 정책 판단이 필요하면 비즈니스 규칙(서비스), 그 외 요청 자체의 형식 문제면 파라미터 검증(컨트롤러)이다.

**Bean Validation을 우선 활용한다.** 단일 필드의 정적 제약(형식·범위·길이)은 `XxxParameterValidator`를 새로 만들기 전에 DTO에 `@field:Min`/`@field:Max`/`@field:Pattern`/`@field:Size` 등을 붙이고 컨트롤러에 `@Valid`를 적용하는 것으로 먼저 해결한다. 커스텀 `XxxParameterValidator`는 updateMask 조건부 필수값처럼 애노테이션만으로 표현할 수 없는 경우에만 작성한다.

```kotlin
// 단순 범위 검증은 Bean Validation으로 (DTO)
data class UpdateRecordEmotionRequest(
    @field:Min(value = 1, message = "감정 강도는 1 이상이어야 합니다.")
    @field:Max(value = 10, message = "감정 강도는 10 이하이어야 합니다.")
    val intensity: Int?,
    val updateMask: List<RecordEmotionUpdateMask>
)

// updateMask 조건부 필수값처럼 애노테이션으로 표현 안 되는 것만 커스텀 Validator로 (컨트롤러에서 호출)
class RecordEmotionParameterValidator {
    fun validate(request: UpdateRecordEmotionRequest) {
        request.updateMask.forEach {
            when (it) {
                RecordEmotionUpdateMask.INTENSITY ->
                    if (request.intensity == null) throw BusinessException(...)
            }
        }
    }
}
```

**엔티티 불변식과의 중복은 의도된 트레이드오프다.** 불변식이 사용자 입력으로 직접 도달 가능하면(예: 자기 자신 팔로우 불가) 상위 레이어에 얇은 사전 체크를 추가로 둔다. 단, 로직을 통째로 재구현하지 말고 한 줄짜리 조기 반환으로 유지한다. 검증은 실제 그 동작이 일어나는 시점(엔티티가 실제로 생성되는 지점)에서만 하면 되고, 나중에 쓰이지도 않는 결과를 얻으려고 미리 당겨서 호출하지 않는다.

> 근거: 엔티티 불변식은 컨트롤러·서비스 검증을 우회하는 다른 진입 경로(배치 잡, 다른 서비스, 향후 추가되는 기능)에서도 항상 지켜져야 하는 최후 방어선이다. 다만 `require()` 위반(`IllegalArgumentException`)이 그대로 새어나가면 500으로 응답되므로, 전역 `ExceptionHandler`에 `IllegalArgumentException → 400(INVALID_PARAMETER)` 매핑을 반드시 둬서 상위 레이어에 사전 체크가 없어도 최소한 400은 보장되게 한다.
