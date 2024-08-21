package com.chobolevel.api.advice

import com.chobolevel.api.dto.common.ErrorResponse
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidExceptionHandler(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.info("why not working....")
        val errorCode = ErrorCode.INVALID_PARAMETER
        val status = HttpStatus.BAD_REQUEST
        val message = e.message
        return ResponseEntity.status(status).body(
            ErrorResponse(
                errorCode = errorCode,
                errorMessage = message
            )
        )
    }

    @ExceptionHandler(ApiException::class)
    fun apiExceptionHandler(e: ApiException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode!!
        val status = e.status!!
        val errorMessage = e.message!!
        return ResponseEntity.status(status).body(
            ErrorResponse(
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        )
    }
}
