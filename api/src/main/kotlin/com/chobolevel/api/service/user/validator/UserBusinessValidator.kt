package com.chobolevel.api.service.user.validator

import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserBusinessValidator(
    private val userFinder: UserFinder,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    fun validateEmailExists(email: String) {
        if (userFinder.existsByEmail(email = email)) {
            throw ApiException(
                errorCode = ErrorCode.USER_EMAIL_ALREADY_EXISTS,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 이메일입니다."
            )
        }
    }

    fun validateNicknameExists(nickname: String) {
        if (userFinder.existsByNickname(nickname = nickname)) {
            throw ApiException(
                errorCode = ErrorCode.USER_NICKNAME_ALREADY_EXISTS,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 닉네임입니다."
            )
        }
    }

    fun validatePasswordMatch(rawPassword: String, encodedPassword: String) {
        if (passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw ApiException(
                errorCode = ErrorCode.USER_PASSWORD_NOT_MATCH,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 이메일입니다."
            )
        }
    }

    fun validatePasswordReuse(encodedCurPassword: String, newPassword: String) {
        if (passwordEncoder.matches(encodedCurPassword, newPassword)) {
            throw ApiException(
                errorCode = ErrorCode.USER_PASSWORD_REUSE_NOT_ALLOWED,
                status = HttpStatus.BAD_REQUEST,
                message = "동일한 비밀번호는 사용할 수 없습니다."
            )
        }
    }
}
