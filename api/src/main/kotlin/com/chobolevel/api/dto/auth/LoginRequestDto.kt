package com.chobolevel.api.dto.auth

import com.chobolevel.domain.entity.user.UserLoginType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginRequestDto(
    val email: String,
    val password: String,
    val loginType: UserLoginType
)
