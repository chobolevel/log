package com.chobolevel.api.common.advice

import com.chobolevel.domain.common.exception.ErrorType
import org.springframework.http.HttpStatus

// ErrorType(도메인의 의미 분류) -> HTTP 상태 매핑은 api 계층의 이 한 곳에서만 한다.
// when 표현식이라 ErrorType이 추가되면 여기서 컴파일 오류가 나 매핑 누락을 막는다.
fun ErrorType.toHttpStatus(): HttpStatus = when (this) {
    ErrorType.INVALID -> HttpStatus.BAD_REQUEST
    ErrorType.UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED
    ErrorType.FORBIDDEN -> HttpStatus.FORBIDDEN
    ErrorType.NOT_FOUND -> HttpStatus.NOT_FOUND
    ErrorType.CONFLICT -> HttpStatus.CONFLICT
}
