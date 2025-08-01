package com.chobolevel.api.advice

import com.chobolevel.api.dto.common.ErrorResponse
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
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
        val errorCode: ErrorCode = ErrorCode.INVALID_PARAMETER
        val message: String = e.bindingResult.allErrors[0].defaultMessage ?: "유효하지 않은 파라미터가 있습니다."
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                errorCode = errorCode,
                errorMessage = message
            )
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableExceptionHandler(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorCode: ErrorCode = ErrorCode.INVALID_FORMAT
        val message = "요청 데이터 형식이 유효하지 않습니다."
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                errorCode = errorCode,
                errorMessage = message
            )
        )
    }

    @ExceptionHandler(ApiException::class)
    fun apiExceptionHandler(e: ApiException): ResponseEntity<ErrorResponse> {
        val errorCode: ErrorCode = e.errorCode!!
        val status: HttpStatus = e.status!!
        val errorMessage: String = e.message!!
        return ResponseEntity.status(status).body(
            ErrorResponse(
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(errorCode = ErrorCode.UNKNOWN_ERROR, errorMessage = e.message ?: "알 수 없는 에러입니다.")
        logger.error("[(${request.method}) ${request.requestURL} ] Internal server error: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
