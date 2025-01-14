package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserValidator {

    private final val emailRegexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$".toRegex()
    private final val passwordRegexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$".toRegex()
    private final val nicknameRegexp = "^[a-zA-Z가-힣]+\$".toRegex()

    fun validate(request: CreateUserRequestDto) {
        if (!emailRegexp.matches(request.email)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이메일 형식이 올바르지 않습니다."
            )
        }
        if (!request.nickname.matches(nicknameRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "닉네임은 영어 또는 한글만 사용할 수 있습니다."
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

    fun validate(request: UpdateUserRequestDto) {
        request.updateMask.forEach {
            when (it) {
                UserUpdateMask.NICKNAME -> {
                    if (request.nickname.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 닉네임이 유효하지 않습니다."
                        )
                    }
                    if (!request.nickname.matches(nicknameRegexp)) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "닉네임은 영어 또는 한글만 사용할 수 있습니다."
                        )
                    }
                }
            }
        }
    }
}
