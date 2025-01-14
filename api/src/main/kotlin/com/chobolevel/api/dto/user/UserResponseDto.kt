package com.chobolevel.api.dto.user

import com.chobolevel.api.dto.user.image.UserImageResponseDto
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserRoleType
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
