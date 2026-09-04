package com.chobolevel.api.user.validator

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.ResetUserPasswordRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserUpdateMask
import org.springframework.stereotype.Component

@Component
class UserBusinessValidator(
    private val userRepository: UserRepository,
    private val passwordProvider: PasswordProvider,
    private val cacheProvider: CacheProvider
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
            throw InvalidParameterException(
                errorCode = ErrorCode.USER_PASSWORD_NOT_MATCHED
            )
        }

        // 현재 비밀번호랑 입력한 변경할 비밀번호 일치 여부
        if (passwordProvider.matches(
                plainText = request.newPassword,
                encodedText = user.password
            )
        ) {
            throw PolicyViolationException(
                errorCode = ErrorCode.USER_PASSWORD_REUSING_NOT_ALLOWED
            )
        }
    }

    fun validate(request: ResetUserPasswordRequest) {
        validateEmailNotExists(email = request.email)
        // 비밀번호 초기화 코드 검증
        val cachedCode: String = cacheProvider.get("${CacheKeyPrefix.RESET_PASSWORD}${request.email}") ?: throw InvalidParameterException(
            errorCode = ErrorCode.RESET_USER_PASSWORD_CODE_NOT_EXISTS
        )
        if (cachedCode != request.code) {
            throw InvalidParameterException(
                errorCode = ErrorCode.USER_PASSWORD_NOT_MATCHED
            )
        }
    }

    fun validate(request: SendEmailVerificationCodeRequest) {
        validateEmailNotExists(email = request.email)
    }

    private fun validateEmailExists(email: String) {
        if (userRepository.existsByEmail(email = email)) {
            throw PolicyViolationException(
                errorCode = ErrorCode.USER_EMAIL_ALREADY_EXISTS
            )
        }
    }

    private fun validateEmailNotExists(email: String) {
        if (!userRepository.existsByEmail(email = email)) {
            throw InvalidParameterException(
                errorCode = ErrorCode.USER_EMAIL_NOT_EXISTS
            )
        }
    }

    private fun validateNicknameExists(nickname: String) {
        if (userRepository.existsByNickname(nickname = nickname)) {
            throw PolicyViolationException(
                errorCode = ErrorCode.USER_NICKNAME_ALREADY_EXISTS
            )
        }
    }
}
