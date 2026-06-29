package com.chobolevel.api.user.dto

import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.entity.UserRoleType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserResponseDto(
    val id: Long = 0,
    val email: String = "",
    val loginType: UserLoginType = UserLoginType.GENERAL,
    val nickname: String = "",
    val role: UserRoleType = UserRoleType.ROLE_USER,
    val profileImage: UserImageResponseDto? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
