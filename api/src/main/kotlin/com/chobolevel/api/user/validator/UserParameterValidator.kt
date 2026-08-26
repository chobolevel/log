package com.chobolevel.api.user.validator

import com.chobolevel.api.common.constant.Regexp
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserUpdateMask
import org.springframework.stereotype.Component

@Component
class UserParameterValidator {

    fun validate(request: CreateUserRequest) {
        validateEmailFormat(email = request.email)
        validateNicknameFormat(nickname = request.nickname)
        if (request.loginType == UserLoginType.GENERAL) {
            validatePasswordFormat(password = request.password!!)
        }
    }

    fun validate(request: UpdateUserRequest) {
        request.updateMask.forEach {
            when (it) {
                UserUpdateMask.NICKNAME -> {
                    if (request.nickname.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
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
        if (!email.matches(Regexp.EMAIL_REGEXP)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "이메일 형식이 올바르지 않습니다."
            )
        }
    }

    private fun validateNicknameFormat(nickname: String) {
        if (!nickname.matches(Regexp.NICKNAME_REGEXP)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "닉네임은 영어 또는 한글만 사용할 수 있습니다."
            )
        }
    }

    private fun validatePasswordFormat(password: String) {
        if (!password.matches(Regexp.PASSWORD_REGEXP)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "비밀번호는 영문 + 숫자 + 특수문자 조합으로 8자리 이상이어야 합니다."
            )
        }
    }
}
