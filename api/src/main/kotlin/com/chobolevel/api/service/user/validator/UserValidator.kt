package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.ChangeUserPasswordRequest
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserValidator(
    private val finder: UserFinder,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    companion object {
        private val emailRegexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$".toRegex()
        private val passwordRegexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$".toRegex()
        private val nicknameRegexp = "^[a-zA-Z가-힣]+\$".toRegex()
    }

    fun validate(request: CreateUserRequestDto) {
        if (!request.email.matches(emailRegexp)) {
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
        if (request.loginType == UserLoginType.GENERAL && !request.password!!.matches(passwordRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "비밀번호는 영문 + 숫자 + 특수문자 조합으로 8자리 이상이어야 합니다."
            )
        }
        if (finder.existsByEmail(request.email)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 이메일입니다."
            )
        }
        if (finder.existsByNickname(request.nickname)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 닉네임입니다."
            )
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

    fun validate(request: ChangeUserPasswordRequest, entity: User) {
        if (!passwordEncoder.matches(request.curPassword, entity.password)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "현재 비밀번호가 일치하지 않습니다."
            )
        }
        if (request.curPassword == request.newPassword) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "현재 비밀번호와 같은 비밀번호로 변경할 수 없습니다."
            )
        }
        if (!request.newPassword.matches(passwordRegexp)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "비밀번호는 영문 + 숫자 + 특수문자 조합으로 8자리 이상이어야 합니다."
            )
        }
    }
}
