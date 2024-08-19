package com.chobolevel.domain.exception

import org.springframework.http.HttpStatus


class ApiException(
    val errorCode: ErrorCode? = ErrorCode.UNKNOWN_ERROR,
    val status: HttpStatus? = HttpStatus.BAD_REQUEST,
    override val message: String? = null,
    val throwable: Throwable? = null
) : RuntimeException(message, throwable)
