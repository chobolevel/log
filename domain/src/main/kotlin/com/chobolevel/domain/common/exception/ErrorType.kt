package com.chobolevel.domain.common.exception

// 비즈니스 예외(4xx)의 의미 분류. HTTP를 모르는 도메인 관점의 분류이며, 상태 코드 매핑은 api 계층이 한 곳에서 한다.
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
enum class ErrorType {
    INVALID,
    UNAUTHENTICATED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT
}
