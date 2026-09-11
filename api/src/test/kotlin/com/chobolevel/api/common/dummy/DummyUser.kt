package com.chobolevel.api.common.dummy

import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.ResetUserPasswordRequest
import com.chobolevel.api.user.dto.SendUserPasswordResetEmailRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.api.user.dto.UserSummaryResponse
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import com.chobolevel.domain.user.vo.UserUpdateMask

object DummyUser {
    const val ID: Long = 1L
    const val EMAIL: String = "test@test.com"
    const val PASSWORD: String = "encodedPassword!"
    const val NICKNAME: String = "testUser"
    const val RESET_CODE: String = "resetCode123"

    fun toEntity(): User = User(
        email = EMAIL,
        password = PASSWORD,
        socialId = null,
        loginType = UserLoginType.GENERAL,
        nickname = NICKNAME,
        role = UserRoleType.ROLE_USER
    ).also { it.id = ID }

    fun toCreateRequest(): CreateUserRequest = CreateUserRequest(
        email = EMAIL,
        password = PASSWORD,
        nickname = NICKNAME,
    )

    fun toUpdateRequest(): UpdateUserRequest = UpdateUserRequest(
        nickname = "newNickname",
        updateMask = listOf(UserUpdateMask.NICKNAME)
    )

    fun toChangePasswordRequest(): ChangeUserPasswordRequest = ChangeUserPasswordRequest(
        curPassword = PASSWORD,
        newPassword = "newPassword"
    )

    fun toSendResetPasswordEmailRequest(): SendUserPasswordResetEmailRequest = SendUserPasswordResetEmailRequest(
        email = EMAIL
    )

    fun toResetPasswordRequest(): ResetUserPasswordRequest = ResetUserPasswordRequest(
        email = EMAIL,
        code = RESET_CODE,
        password = "NewPassword123!"
    )

    fun toResponse(): UserResponse = UserResponse(
        id = ID,
        email = EMAIL,
        loginType = UserLoginType.GENERAL,
        nickname = NICKNAME,
        role = UserRoleType.ROLE_USER,
        profileImage = null,
        createdAt = 0L,
        updatedAt = 0L
    )

    fun toSummaryResponse(): UserSummaryResponse = UserSummaryResponse(
        id = ID,
        nickname = NICKNAME,
        profileImage = null
    )
}
