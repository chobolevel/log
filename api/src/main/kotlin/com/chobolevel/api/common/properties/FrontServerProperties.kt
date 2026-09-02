package com.chobolevel.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "front-server")
data class FrontServerProperties(
    val host: String,
    val resetPasswordPath: String
)
