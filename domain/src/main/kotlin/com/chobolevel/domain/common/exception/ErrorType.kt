package com.chobolevel.domain.common.exception

import org.springframework.http.HttpStatus

// 비즈니스 예외(4xx)의 의미 분류. 의미와 그 의미가 HTTP로 전달될 때의 상태를 한 곳에 함께 둔다.
// 상태를 HttpStatus로 직접 들고 있으므로 spring-web은 domain의 공개 API의 일부다(domain/build.gradle.kts의 api 선언 참고).
//
// 새 에러코드의 type은 아래 질문을 위에서부터 물어 처음 "예"가 나오는 곳으로 정한다.
//   1. 신원을 확인하지 못했나? (토큰 없음·만료·위조, 자격증명 불일치)             -> UNAUTHENTICATED
//   2. 신원은 확인됐지만 권한·소유권이 없나?                                       -> FORBIDDEN
//   3. 클라이언트가 지목한 대상(경로 변수, ID)이 없나?                              -> NOT_FOUND
//   4. 요청 값이 현재 상태와 무관하게 잘못됐나? (형식, 범위, 자기 자신)             -> INVALID
//   5. 값은 유효하지만 현재 상태 때문에 수행할 수 없나? (유일성 충돌, 사용 중, 상태 전이 불가) -> CONFLICT
// 핵심 구분은 4번과 5번이다. 값 자체가 틀렸으면 INVALID, 상태 때문에 안 되면 CONFLICT.
//
// 5xx(우리 시스템의 일시 장애, 외부 시스템 실패)는 여기에 두지 않는다. 요청자가 고칠 수 있는 문제만 4xx이며,
// 5xx는 예외 클래스(InternalSystemException, ExternalSystemException)가 상태를 정한다.
enum class ErrorType(val httpStatus: HttpStatus) {
    INVALID(HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT);

    init {
        // BusinessException은 4xx 전용이다. 5xx 상태가 섞이지 않도록 불변식으로 막는다.
        require(httpStatus.is4xxClientError) { "ErrorType은 4xx 상태만 가질 수 있습니다: $name($httpStatus)" }
    }
}
