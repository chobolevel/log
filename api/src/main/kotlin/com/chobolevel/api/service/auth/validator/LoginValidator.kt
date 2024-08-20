package com.chobolevel.api.service.auth.validator

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class LoginValidator : LoginValidatable {

    override fun validate(request: LoginRequestDto) {
        if (request.loginType != UserLoginType.GENERAL) {
            if (request.socialId.isNullOrEmpty()) {
                throw ApiException(
                    errorCode = ErrorCode.INVALID_PARAMETER,
                    status = HttpStatus.BAD_REQUEST,
                    message = "소셜 로그인 시 소셜 아이디는 필수 값입니다."
                )
            }
        } else {
            if (request.email.isNullOrEmpty()) {
                throw ApiException(
                    errorCode = ErrorCode.INVALID_PARAMETER,
                    status = HttpStatus.BAD_REQUEST,
                    message = "로그인 시 이메일은 필수 값입니다."
                )
            }
            if (request.password.isNullOrEmpty()) {
                throw ApiException(
                    errorCode = ErrorCode.INVALID_PARAMETER,
                    status = HttpStatus.BAD_REQUEST,
                    message = "로그인 시 비밀번호는 필수 값입니다."
                )
            }
        }
    }
}
