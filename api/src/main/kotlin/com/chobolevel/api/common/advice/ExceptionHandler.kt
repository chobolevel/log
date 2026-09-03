package com.chobolevel.api.common.advice

import com.chobolevel.api.common.dto.ErrorResponse
import com.chobolevel.domain.common.exception.BadCredentialException
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ExternalApiException
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.common.exception.UnAuthorizedException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(BadCredentialException::class)
    fun handleBadCredentialException(e: BadCredentialException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(UnAuthorizedException::class)
    fun handleUnAuthorizedException(e: UnAuthorizedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(DataNotFoundException::class)
    fun handleDataNotFoundException(e: DataNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(PolicyViolationException::class)
    fun handlePolicyViolationException(e: PolicyViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(InvalidParameterException::class)
    fun handleInvalidParameterException(e: InvalidParameterException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
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
        val errorCode: ErrorCode = ErrorCode.INVALID_REQUEST_FORMAT
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                errorCode = errorCode,
                errorMessage = errorCode.defaultMessage
            )
        )
    }

    @ExceptionHandler(ExternalApiException::class)
    fun handleExternalApiException(e: ExternalApiException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("[(${request.method}) ${request.requestURL}] External API error: ${e.message}", e.throwable ?: e)
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
            ErrorResponse(errorCode = e.errorCode, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(errorCode = ErrorCode.INTERNAL_SERVER_ERROR, errorMessage = e.message ?: "알 수 없는 에러입니다.")
        logger.error("[(${request.method}) ${request.requestURL} ] Internal server error: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
