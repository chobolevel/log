package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.ChangeUserPasswordRequest
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserParameterValidator {

    companion object {
        private val emailRegexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$".toRegex()
        private val passwordRegexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$".toRegex()
        private val nicknameRegexp = "^[a-zA-Z가-힣]+\$".toRegex()
    }

    fun validate(request: CreateUserRequestDto) {
        validateEmailFormat(email = request.email)
        validateNicknameFormat(nickname = request.nickname)
        if (request.loginType == UserLoginType.GENERAL) {
            validatePasswordFormat(password = request.password!!)
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
                    validateNicknameFormat(nickname = request.nickname)
                }
            }
        }
    }

    fun validate(request: ChangeUserPasswordRequest) {
        validatePasswordFormat(password = request.newPassword)
    }

    private fun validateEmailFormat(email: String) {
        if (!email.matches(emailRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이메일 형식이 올바르지 않습니다."
            )
        }
    }

    private fun validateNicknameFormat(nickname: String) {
        if (!nickname.matches(nicknameRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "닉네임은 영어 또는 한글만 사용할 수 있습니다."
            )
        }
    }

    private fun validatePasswordFormat(password: String) {
        if (!password.matches(passwordRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "비밀번호는 영문 + 숫자 + 특수문자 조합으로 8자리 이상이어야 합니다."
            )
        }
    }
}
