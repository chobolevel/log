package com.chobolevel.api.user.dto

import java.util.Date

data class JwtResponse(
    val accessToken: String,
    val accessTokenExpiredAt: Date,
    val refreshToken: String,
    val refreshTokenExpiredAt: Date,
)
