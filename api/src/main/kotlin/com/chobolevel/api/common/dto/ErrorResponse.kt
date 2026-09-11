package com.chobolevel.api.common.dto

import com.chobolevel.domain.common.exception.ErrorCode

data class ErrorResponse(
    val errorCode: ErrorCode,
    val errorMessage: String
)
