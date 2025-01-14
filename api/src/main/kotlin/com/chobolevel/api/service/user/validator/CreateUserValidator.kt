package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class CreateUserValidator : CreateUserValidatable {

    private final val emailRegexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$".toRegex()
    private final val passwordRegexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$".toRegex()

    override fun validate(request: CreateUserRequestDto) {
        if (!emailRegexp.matches(request.email)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이메일 형식이 올바르지 않습니다."
            )
        }
        when (request.loginType) {
            UserLoginType.GENERAL -> {
                if (request.password.isNullOrBlank()) {
                    throw ApiException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        status = HttpStatus.BAD_REQUEST,
                        message = "회원가입 시 비밀번호는 필수 값입니다."
                    )
                }
                if (!request.password.matches(passwordRegexp)) {
                    throw ApiException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        message = "비밀번호는 영문 + 숫자 + 특수문자 조합으로 8자리 이상이어야 합니다."
                    )
                }
            }

            else -> {
                if (request.socialId.isNullOrEmpty()) {
                    throw ApiException(
                        errorCode = ErrorCode.INVALID_PARAMETER,
                        status = HttpStatus.BAD_REQUEST,
                        message = "소셜회원가입 시 소셜 아이디는 필수 값입니다."
                    )
                }
            }
        }
    }
}
