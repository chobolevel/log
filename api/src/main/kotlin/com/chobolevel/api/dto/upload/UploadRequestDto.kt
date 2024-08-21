package com.chobolevel.api.dto.upload

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UploadRequestDto(
    val prefix: String,
    val filename: String,
    val extension: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UploadResponseDto(
    val url: String,
    val filenameWithExtension: String
)
