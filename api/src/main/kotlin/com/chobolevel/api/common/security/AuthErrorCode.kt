package com.chobolevel.api.common.security

import com.chobolevel.domain.common.exception.ErrorCode

// 인증은 토큰을 다루는 api 계층의 관심사라 domain이 아닌 api가 소유한다. 인터페이스는 domain 것을 구현한다.
enum class AuthErrorCode(override val defaultMessage: String) : ErrorCode {
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    BAD_CREDENTIALS("유효하지 않은 접근입니다.")
}
