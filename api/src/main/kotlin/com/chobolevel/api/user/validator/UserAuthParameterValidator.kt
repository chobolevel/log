package com.chobolevel.api.user.validator

import com.chobolevel.api.common.constant.Regexp
import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.LoginRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import org.springframework.stereotype.Component

@Component
class UserAuthParameterValidator {

    fun validate(request: LoginRequest) {
        validateEmail(request.email)
    }

    fun validate(request: SendEmailVerificationCodeRequest) {
        validateEmail(request.email)
    }

    fun validate(request: CheckEmailVerificationCodeRequest) {
        validateEmail(request.email)
        if (request.verificationCode.length != 13) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "인증 코드는 13자리입니다."
            )
        }
    }

    private fun validateEmail(email: String) {
        if (!email.matches(Regexp.EMAIL_REGEXP)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "이메일 형식이 올바르지 않습니다."
            )
        }
    }
}
