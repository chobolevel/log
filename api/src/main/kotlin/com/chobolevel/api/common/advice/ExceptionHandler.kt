package com.chobolevel.api.common.advice

import com.chobolevel.api.common.dto.ErrorResponse
import com.chobolevel.api.common.security.AuthErrorCode
import com.chobolevel.domain.common.exception.BadCredentialException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ExternalApiException
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.common.exception.SystemErrorCode
import com.chobolevel.domain.common.exception.UnAuthorizedException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(UnAuthorizedException::class)
    fun handleUnAuthorizedException(e: UnAuthorizedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(DataNotFoundException::class)
    fun handleDataNotFoundException(e: DataNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(PolicyViolationException::class)
    fun handlePolicyViolationException(e: PolicyViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(InvalidParameterException::class)
    fun handleInvalidParameterException(e: InvalidParameterException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    // 도메인 엔티티의 require()로 표현된 불변식 위반 — 잡히지 않으면 500으로 새던 것을 400으로 전환한다.
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(errorCode = CommonErrorCode.INVALID_PARAMETER.name, errorMessage = e.message ?: CommonErrorCode.INVALID_PARAMETER.defaultMessage)
        )
    }

    // 사전 중복 검증을 통과한 두 요청이 동시에 들어와 DB unique 제약에서 걸리는 경합(race) 상황의 안전망.
    // 특정 도메인에 한정되지 않으므로 공용 에러코드로 응답한다.
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(errorCode = CommonErrorCode.DUPLICATE_REQUEST.name, errorMessage = CommonErrorCode.DUPLICATE_REQUEST.defaultMessage)
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            errorCode = CommonErrorCode.ACCESS_DENIED.name,
            errorMessage = e.message ?: "접근 권한이 없습니다."
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialException(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            errorCode = AuthErrorCode.BAD_CREDENTIALS.name,
            errorMessage = e.message ?: "유효하지 않은 접근입니다."
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidExceptionHandler(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorCode: ErrorCode = CommonErrorCode.INVALID_PARAMETER
        val message: String = e.bindingResult.allErrors[0].defaultMessage ?: "유효하지 않은 파라미터가 있습니다."
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                errorCode = errorCode.name,
                errorMessage = message
            )
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableExceptionHandler(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorCode: ErrorCode = CommonErrorCode.INVALID_REQUEST_FORMAT
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                errorCode = errorCode.name,
                errorMessage = errorCode.defaultMessage
            )
        )
    }

    @ExceptionHandler(ExternalApiException::class)
    fun handleExternalApiException(e: ExternalApiException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("[(${request.method}) ${request.requestURL}] External API error: ${e.message}", e.throwable ?: e)
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(errorCode = SystemErrorCode.INTERNAL_SERVER_ERROR.name, errorMessage = e.message ?: "알 수 없는 에러입니다.")
        logger.error("[(${request.method}) ${request.requestURL} ] Internal server error: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
