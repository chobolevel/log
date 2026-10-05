package com.chobolevel.api.common.advice

import com.chobolevel.api.common.dto.ErrorResponse
import com.chobolevel.api.common.security.AuthErrorCode
import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.common.exception.ExternalSystemException
import com.chobolevel.domain.common.exception.InternalSystemException
import com.chobolevel.domain.common.exception.SystemErrorCode
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    private val authenticationTrustResolver: AuthenticationTrustResolver = AuthenticationTrustResolverImpl()

    // 예상된 비즈니스 실패(4xx). 상태는 errorCode.type으로 결정한다(ErrorType.toHttpStatus).
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        return businessResponse(errorCode = e.errorCode, message = e.message)
    }

    // 우리가 운영하는 시스템의 예상된 일시 장애(503). 상태는 클래스가 정한다.
    @ExceptionHandler(InternalSystemException::class)
    fun handleInternalSystemException(e: InternalSystemException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    // 외부 시스템 실패(502)
    @ExceptionHandler(ExternalSystemException::class)
    fun handleExternalSystemException(e: ExternalSystemException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("[(${request.method}) ${request.requestURL}] External system error: ${e.message}", e.cause ?: e)
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
            ErrorResponse(errorCode = e.errorCode.name, errorMessage = e.message ?: e.errorCode.defaultMessage)
        )
    }

    // 도메인 엔티티의 require()로 표현된 불변식 위반 — 잡히지 않으면 500으로 새던 것을 400으로 전환한다.
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return businessResponse(errorCode = CommonErrorCode.INVALID_PARAMETER, message = e.message)
    }

    // 사전 중복 검증을 통과한 두 요청이 동시에 들어와 DB unique 제약에서 걸리는 경합(race) 상황의 안전망.
    // 특정 도메인에 한정되지 않으므로 공용 에러코드로 응답한다.
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        return businessResponse(errorCode = CommonErrorCode.DUPLICATE_REQUEST)
    }

    // 메서드 시큐리티(@PreAuthorize)는 익명 요청과 권한 부족 요청 모두 같은 예외를 던지므로, 인증 여부로 구분한다.
    // 인증되지 않았으면 401(로그인 필요), 인증은 됐지만 권한이 없으면 403.
    // 응답 메시지는 Spring 기본 영문 메시지("Access Denied")를 노출하지 않고 에러코드의 기본 메시지를 쓴다.
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        val isAnonymous: Boolean = authentication == null || authenticationTrustResolver.isAnonymous(authentication)
        return if (isAnonymous) {
            businessResponse(errorCode = AuthErrorCode.AUTHENTICATION_REQUIRED)
        } else {
            businessResponse(errorCode = CommonErrorCode.ACCESS_DENIED)
        }
    }

    // Spring 기본 영문 메시지("Bad credentials")를 노출하지 않고 에러코드의 기본 메시지를 쓴다.
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return businessResponse(errorCode = AuthErrorCode.BAD_CREDENTIALS)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidExceptionHandler(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message: String = e.bindingResult.allErrors[0].defaultMessage ?: "유효하지 않은 파라미터가 있습니다."
        return businessResponse(errorCode = CommonErrorCode.INVALID_PARAMETER, message = message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableExceptionHandler(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return businessResponse(errorCode = CommonErrorCode.INVALID_REQUEST_FORMAT)
    }

    // 예상하지 못한 실패. 응답에는 내부 예외 메시지(DB 접속 정보, 쿼리 등이 섞일 수 있음)를 담지 않고 고정 메시지만 준다.
    // 상세 원인은 로그에만 남긴다.
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val error: ErrorResponse = ErrorResponse(
            errorCode = SystemErrorCode.INTERNAL_SERVER_ERROR.name,
            errorMessage = SystemErrorCode.INTERNAL_SERVER_ERROR.defaultMessage
        )
        logger.error("[(${request.method}) ${request.requestURL} ] Internal server error: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    private fun businessResponse(errorCode: BusinessErrorCode, message: String? = null): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(errorCode.type.toHttpStatus()).body(
            ErrorResponse(errorCode = errorCode.name, errorMessage = message ?: errorCode.defaultMessage)
        )
    }
}
