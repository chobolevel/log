package com.chobolevel.api.upload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UploadResponse(
    val presignedUrl: String,
    val url: String,
    val filenameWithExtension: String
)
