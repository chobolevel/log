package com.chobolevel.api.common.security
import java.util.Date

data class JwtResponse(
    val accessToken: String,
    val accessTokenExpiredAt: Date,
    val refreshToken: String,
    val refreshTokenExpiredAt: Date,
)
