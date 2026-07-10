package com.chobolevel.api.user.validator

import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserUpdateMask
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserBusinessValidator(
    private val userRepository: UserRepository,
    private val passwordProvider: PasswordProvider
) {

    fun validate(request: CreateUserRequest) {
        validateEmailExists(email = request.email)
        validateNicknameExists(nickname = request.nickname)
    }

    fun validate(request: UpdateUserRequest) {
        if (request.updateMask.contains(UserUpdateMask.NICKNAME)) {
            validateNicknameExists(nickname = request.nickname!!)
        }
    }

    fun validate(user: User, request: ChangeUserPasswordRequest) {
        // 현재 비밀번호랑 입력한 현재 비밀번호 일치 여부
        if (!passwordProvider.matches(
                plainText = request.curPassword,
                encodedText = user.password
            )
        ) {
            throw ApiException(
                errorCode = ErrorCode.USER_PASSWORD_NOT_MATCH,
                message = "비밀번호가 일치하지 않습니다."
            )
        }

        // 현재 비밀번호랑 입력한 변경할 비밀번호 일치 여부
        if (!passwordProvider.matches(
                plainText = request.newPassword,
                encodedText = user.password
            )
        ) {
            throw ApiException(
                errorCode = ErrorCode.USER_PASSWORD_REUSE_NOT_ALLOWED,
                message = "동일한 비밀번호로 설정할 수 없습니다."
            )
        }
    }

    private fun validateEmailExists(email: String) {
        if (userRepository.existsByEmail(email = email)) {
            throw ApiException(
                errorCode = ErrorCode.USER_EMAIL_ALREADY_EXISTS,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 이메일입니다."
            )
        }
    }

    private fun validateNicknameExists(nickname: String) {
        if (userRepository.existsByNickname(nickname = nickname)) {
            throw ApiException(
                errorCode = ErrorCode.USER_NICKNAME_ALREADY_EXISTS,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 닉네임입니다."
            )
        }
    }
}
