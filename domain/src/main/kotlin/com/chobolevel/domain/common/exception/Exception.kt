package com.chobolevel.domain.common.exception

// [전환용] 아래 클래스들은 BusinessException으로 치환하면서 제거한다(throw 지점과 테스트 치환 단계).
// 지금은 BusinessException의 하위 클래스이므로 HTTP 상태는 클래스가 아니라 errorCode.type이 결정한다.

open class InvalidParameterException(
    errorCode: BusinessErrorCode,
    message: String? = null
) : BusinessException(errorCode, message)

open class PolicyViolationException(
    errorCode: BusinessErrorCode,
    message: String? = null
) : BusinessException(errorCode, message)

open class BadCredentialException(
    errorCode: BusinessErrorCode,
    message: String? = null
) : BusinessException(errorCode, message)

open class UnAuthorizedException(
    errorCode: BusinessErrorCode,
    message: String? = null
) : BusinessException(errorCode, message)

open class ForbiddenException(
    errorCode: BusinessErrorCode,
    message: String? = null
) : BusinessException(errorCode, message)

open class DataNotFoundException(
    errorCode: BusinessErrorCode,
    message: String? = null
) : BusinessException(errorCode, message)
