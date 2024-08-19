package com.chobolevel.api.dto.user

import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserRoleType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserResponseDto(
    val id: Long,
    val email: String,
    val loginType: UserLoginType,
    val nickname: String,
    val phone: String,
    val role: UserRoleType,
    val createdAt: Long,
    val updatedAt: Long
)
