package com.chobolevel.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "resend")
data class ResendProperties(
    val apiKey: String
)
