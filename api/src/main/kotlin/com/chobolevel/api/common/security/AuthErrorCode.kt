package com.chobolevel.api.common.security

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorType

// 인증은 토큰을 다루는 api 계층의 관심사라 domain이 아닌 api가 소유한다. 인터페이스는 domain 것을 구현한다.
enum class AuthErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    INVALID_TOKEN(ErrorType.UNAUTHENTICATED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(ErrorType.UNAUTHENTICATED, "만료된 토큰입니다."),
    BAD_CREDENTIALS(ErrorType.UNAUTHENTICATED, "유효하지 않은 접근입니다.")
}
