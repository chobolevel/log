package com.chobolevel.api.advice

import com.chobolevel.api.dto.common.ErrorResponse
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(e: org.springframework.security.access.AccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.ACCESS_DENIED,
            errorMessage = e.message ?: "접근 권한이 없습니다."
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialException(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            errorCode = ErrorCode.BAD_CREDENTIALS,
            errorMessage = e.message ?: "유효하지 않은 접근입니다."
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

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

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(errorCode = ErrorCode.UNKNOWN_ERROR, errorMessage = e.message ?: "알 수 없는 에러입니다.")
        logger.error("Internal server error: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
