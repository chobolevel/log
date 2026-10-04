package com.chobolevel.domain.common.exception

/**
 * 우리가 운영하는 시스템(Redis, DB, Kafka, 분산락 등)이 일시적으로 요청을 처리하지 못하는 "예상된" 장애(503).
 *
 * 이름의 Internal은 "우리가 운영하는"이라는 뜻이며, 500 Internal Server Error가 아니다.
 * 예상하지 못한 실패는 별도 클래스 없이 일반 예외로 던져져 핸들러가 500으로 처리한다.
 * 외부 서비스(이메일 등)의 실패는 [ExternalSystemException]을 사용한다.
 *
 * - 재시도하면 성공할 수 있으므로 Kafka 재시도 대상에서 제외하지 않는다.
 * - 4xx로 응답해야 하는 요청자 책임의 실패는 [BusinessException]을 사용한다.
 */
class InternalSystemException(
    val errorCode: SystemErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.defaultMessage, cause)
