package com.chobolevel.api.common.email

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "resend")
data class ResendProperties(
    val apiKey: String
)
