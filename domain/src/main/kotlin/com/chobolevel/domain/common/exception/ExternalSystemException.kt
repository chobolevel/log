package com.chobolevel.domain.common.exception

/**
 * 외부 시스템(이메일 발송 서비스, 스토리지 등 우리가 운영하지 않는 서비스)의 실패(502).
 *
 * 원인이 되는 외부 예외는 [cause]로 연결한다. 우리가 운영하는 시스템의 일시 장애는 [InternalSystemException]을 사용한다.
 * 재시도하면 성공할 수 있으므로 Kafka 재시도 대상에서 제외하지 않는다.
 */
class ExternalSystemException(
    val errorCode: SystemErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.defaultMessage, cause)
