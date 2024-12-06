package com.chobolevel.api.dto.upload

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UploadRequestDto(
    val prefix: String,
    val filename: String,
    val extension: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UploadResponseDto(
    val presignedUrl: String,
    val url: String,
    val filenameWithExtension: String
)
