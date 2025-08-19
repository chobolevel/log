package com.chobolevel.api.service.auth.validator

import com.chobolevel.api.dto.auth.CheckEmailVerificationCodeRequest
import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.auth.SendEmailVerificationCodeRequest
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class AuthParameterValidator {

    private final val emailRegexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$".toRegex()

    fun validate(request: LoginRequestDto) {
        when (request.loginType) {
            UserLoginType.GENERAL -> {
                if (request.password.isNullOrEmpty()) {
                    throw ApiException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        status = HttpStatus.BAD_REQUEST,
                        message = "로그인 시 비밀번호는 필수 값입니다."
                    )
                }
            }

            else -> {
                if (request.socialId.isNullOrEmpty()) {
                    throw ApiException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        status = HttpStatus.BAD_REQUEST,
                        message = "소셜 로그인 시 소셜 아이디는 필수 값입니다."
                    )
                }
            }
        }
    }

    fun validate(request: SendEmailVerificationCodeRequest) {
        validateEmail(request.email)
    }

    fun validate(request: CheckEmailVerificationCodeRequest) {
        validateEmail(request.email)
        if (request.verificationCode.length != 13) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "인증 코드는 13자리입니다."
            )
        }
    }

    private fun validateEmail(email: String) {
        if (!email.matches(emailRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "이메일 형식이 올바르지 않습니다."
            )
        }
    }
}
