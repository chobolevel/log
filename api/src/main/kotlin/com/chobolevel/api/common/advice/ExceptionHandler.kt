package com.chobolevel.api.common.advice

import com.chobolevel.api.common.dto.ErrorResponse
import com.chobolevel.api.common.security.AuthErrorCode
import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ExternalSystemException
import com.chobolevel.domain.common.exception.InternalSystemException
import com.chobolevel.domain.common.exception.SystemErrorCode
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

// ResponseEntityExceptionHandler를 상속해 Spring MVC 표준 예외(필수 파라미터 누락, 타입 불일치, 405, 406, 415 등)를
// 올바른 상태와 헤더(예: 405의 Allow)로 처리한다. 표준 예외의 응답 본문은 handleExceptionInternal 한 곳에서 만든다.
@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

    private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    private val authenticationTrustResolver: AuthenticationTrustResolver = AuthenticationTrustResolverImpl()

    // 예상된 비즈니스 실패(4xx). 상태는 errorCode.type으로 결정한다(ErrorType.toHttpStatus).
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<Any> {
        return businessResponse(errorCode = e.errorCode, message = e.message)
    }

    // 우리가 운영하는 시스템의 예상된 일시 장애(503). 상태는 클래스가 정한다.
    @ExceptionHandler(InternalSystemException::class)
    fun handleInternalSystemException(e: InternalSystemException): ResponseEntity<Any> {
        return errorResponse(status = HttpStatus.SERVICE_UNAVAILABLE, errorCode = e.errorCode, message = e.message)
    }

    // 외부 시스템 실패(502)
    @ExceptionHandler(ExternalSystemException::class)
    fun handleExternalSystemException(e: ExternalSystemException, request: HttpServletRequest): ResponseEntity<Any> {
        logger.error("[(${request.method}) ${request.requestURL}] External system error: ${e.message}", e.cause ?: e)
        return errorResponse(status = HttpStatus.BAD_GATEWAY, errorCode = e.errorCode, message = e.message)
    }

    // 도메인 엔티티의 require()로 표현된 불변식 위반 — 잡히지 않으면 500으로 새던 것을 400으로 전환한다.
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Any> {
        return businessResponse(errorCode = CommonErrorCode.INVALID_PARAMETER, message = e.message)
    }

    // 사전 중복 검증을 통과한 두 요청이 동시에 들어와 DB unique 제약에서 걸리는 경합(race) 상황의 안전망.
    // 특정 도메인에 한정되지 않으므로 공용 에러코드로 응답한다.
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): ResponseEntity<Any> {
        return businessResponse(errorCode = CommonErrorCode.DUPLICATE_REQUEST)
    }

    // 메서드 시큐리티(@PreAuthorize)는 익명 요청과 권한 부족 요청 모두 같은 예외를 던지므로, 인증 여부로 구분한다.
    // 인증되지 않았으면 401(로그인 필요), 인증은 됐지만 권한이 없으면 403.
    // 응답 메시지는 Spring 기본 영문 메시지("Access Denied")를 노출하지 않고 에러코드의 기본 메시지를 쓴다.
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<Any> {
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
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<Any> {
        return businessResponse(errorCode = AuthErrorCode.BAD_CREDENTIALS)
    }

    // 예상하지 못한 실패. 응답에는 내부 예외 메시지(DB 접속 정보, 쿼리 등이 섞일 수 있음)를 담지 않고 고정 메시지만 준다.
    // 상세 원인은 로그에만 남긴다. (ResponseEntityExceptionHandler.handleException이 final이라 이름을 달리한다)
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception, request: HttpServletRequest): ResponseEntity<Any> {
        logger.error("[(${request.method}) ${request.requestURL} ] Internal server error: ${e.message}", e)
        return errorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, errorCode = SystemErrorCode.INTERNAL_SERVER_ERROR)
    }

    // ===== Spring MVC 표준 예외 =====

    // @Valid 검증 실패. 검증 애노테이션에 적어 둔 메시지를 그대로 보여준다.
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        return errorResponse(
            status = status,
            errorCode = CommonErrorCode.INVALID_PARAMETER,
            message = firstValidationMessage(ex.bindingResult.allErrors),
            headers = headers
        )
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        return errorResponse(status = status, errorCode = CommonErrorCode.INVALID_REQUEST_FORMAT, headers = headers)
    }

    // 그 밖의 표준 예외(필수 파라미터 누락, 타입 불일치, 405, 406, 415 등)의 응답 본문을 만드는 단일 지점.
    // 상태와 헤더는 Spring이 예외별로 정한 값을 그대로 쓰고, 에러코드만 상태로부터 정한다.
    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        if (statusCode.is5xxServerError) {
            logger.error("[${request.getDescription(false)}] ${ex.javaClass.simpleName}: ${ex.message}", ex)
        }
        return errorResponse(status = statusCode, errorCode = errorCodeOf(statusCode), headers = headers)
    }

    private fun errorCodeOf(status: HttpStatusCode): ErrorCode {
        return when (status.value()) {
            HttpStatus.NOT_FOUND.value() -> HttpErrorCode.PATH_NOT_FOUND
            HttpStatus.METHOD_NOT_ALLOWED.value() -> HttpErrorCode.METHOD_NOT_ALLOWED
            HttpStatus.NOT_ACCEPTABLE.value() -> HttpErrorCode.NOT_ACCEPTABLE
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value() -> HttpErrorCode.UNSUPPORTED_MEDIA_TYPE
            else -> if (status.is5xxServerError) SystemErrorCode.INTERNAL_SERVER_ERROR else CommonErrorCode.INVALID_PARAMETER
        }
    }

    // 첫 번째 검증 오류의 메시지. 타입 변환 실패("Failed to convert value of type ...")처럼 Spring이 만든 영문 메시지는
    // 내부 정보라 노출하지 않고 기본 메시지를 쓴다.
    private fun firstValidationMessage(errors: List<ObjectError>): String {
        val error: ObjectError? = errors.firstOrNull()
        val isTypeMismatch: Boolean = error?.code == "typeMismatch"
        return if (error == null || isTypeMismatch) {
            CommonErrorCode.INVALID_PARAMETER.defaultMessage
        } else {
            error.defaultMessage ?: "유효하지 않은 파라미터가 있습니다."
        }
    }

    private fun businessResponse(errorCode: BusinessErrorCode, message: String? = null): ResponseEntity<Any> {
        return errorResponse(status = errorCode.type.toHttpStatus(), errorCode = errorCode, message = message)
    }

    // 모든 에러 응답이 거치는 지점. 요청의 Accept가 JSON이 아니어도(예: 406 상황) 에러 본문을 쓸 수 있도록
    // Content-Type을 JSON으로 고정한다.
    private fun errorResponse(
        status: HttpStatusCode,
        errorCode: ErrorCode,
        message: String? = null,
        headers: HttpHeaders = HttpHeaders()
    ): ResponseEntity<Any> {
        val responseHeaders: HttpHeaders = HttpHeaders()
        responseHeaders.addAll(headers)
        responseHeaders.contentType = MediaType.APPLICATION_JSON
        val body: ErrorResponse = ErrorResponse(errorCode = errorCode.name, errorMessage = message ?: errorCode.defaultMessage)
        return ResponseEntity<Any>(body, responseHeaders, status)
    }
}
