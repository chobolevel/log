package com.chobolevel.api.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud.aws.s3")
data class S3Properties(
    val host: String
)
