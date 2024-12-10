package com.chobolevel.api.dto.jwt

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class JwtResponse(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiredAt: Long,
    val refreshToken: String,
    val refreshTokenExpiredAt: Long,
)
