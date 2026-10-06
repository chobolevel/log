package com.chobolevel.api.common.advice

import com.chobolevel.domain.common.exception.ErrorCode

// HTTP 프로토콜 수준의 오류 코드. 도메인의 비즈니스 규칙과 무관하게 요청 형식·경로·메서드 자체가 맞지 않을 때 쓴다.
// 상태 코드는 Spring MVC가 표준 예외로 결정하므로 BusinessErrorCode(ErrorType)를 구현하지 않는다.
enum class HttpErrorCode(override val defaultMessage: String) : ErrorCode {
    PATH_NOT_FOUND("요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다."),
    NOT_ACCEPTABLE("요청한 응답 형식을 제공할 수 없습니다."),
    UNSUPPORTED_MEDIA_TYPE("지원하지 않는 Content-Type입니다.")
}
