package com.chobolevel.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
    val accessTokenKey: String,
    val refreshTokenKey: String,
    val cookie: Cookie,
) {
    data class Cookie(
        val path: String,
        val maxAge: Int,
        val domain: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val sameSite: String,
    )
}
