package com.chobolevel.api.common.security
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    val allowOrigins: List<String>
)
