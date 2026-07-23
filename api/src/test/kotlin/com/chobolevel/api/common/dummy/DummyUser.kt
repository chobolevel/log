package com.chobolevel.api.common.dummy

import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType

object DummyUser {
    const val ID: Long = 1L
    const val EMAIL: String = "test@test.com"
    const val PASSWORD: String = "encodedPassword!"
    const val NICKNAME: String = "testUser"

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
        socialId = null,
        loginType = UserLoginType.GENERAL,
        nickname = NICKNAME,
    )

    fun toChangePasswordRequest(): ChangeUserPasswordRequest = ChangeUserPasswordRequest(
        curPassword = PASSWORD,
        newPassword = "newPassword"
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
}
