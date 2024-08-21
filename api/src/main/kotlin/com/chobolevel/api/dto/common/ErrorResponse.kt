package com.chobolevel.api.dto.common

import com.chobolevel.domain.exception.ErrorCode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ErrorResponse(
    val errorCode: ErrorCode,
    val errorMessage: String
)
