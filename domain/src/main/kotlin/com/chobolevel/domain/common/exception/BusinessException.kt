package com.chobolevel.domain.common.exception

/**
 * 요청 자체나 비즈니스 규칙 때문에 거절되는 "예상된" 실패(4xx).
 * 요청자가 고칠 수 있는 문제만 이 예외로 던진다.
 *
 * - HTTP 상태는 [BusinessErrorCode.type]으로 결정된다(ErrorType 참고).
 * - 재시도해도 결과가 같으므로 Kafka 재시도 대상에서 제외한다.
 * - 5xx 성격의 실패는 [InternalSystemException] / [ExternalSystemException]을 사용한다.
 */
open class BusinessException(
    val errorCode: BusinessErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.defaultMessage, cause)
