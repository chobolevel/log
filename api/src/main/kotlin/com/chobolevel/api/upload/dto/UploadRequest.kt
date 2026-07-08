package com.chobolevel.api.upload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UploadRequest(
    val prefix: String,
    val filename: String,
    val extension: String
)
