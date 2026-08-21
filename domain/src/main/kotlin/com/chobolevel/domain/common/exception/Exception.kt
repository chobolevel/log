package com.chobolevel.domain.common.exception

open class LogException(
    open val errorCode: ErrorCode,
    override val message: String?,
    open val throwable: Throwable? = null
) : RuntimeException(message)

open class InvalidParameterException(
    override val errorCode: ErrorCode,
    override val message: String? = null,
    override val throwable: Throwable? = null
) : LogException(errorCode, message ?: errorCode.defaultMessage, throwable)

open class PolicyViolationException(
    override val errorCode: ErrorCode,
    override val message: String? = null,
    override val throwable: Throwable? = null
) : LogException(errorCode, message ?: errorCode.defaultMessage, throwable)

open class BadCredentialException(
    override val errorCode: ErrorCode,
    override val message: String? = null,
    override val throwable: Throwable? = null
) : LogException(errorCode, message ?: errorCode.defaultMessage, throwable)

open class UnAuthorizedException(
    override val errorCode: ErrorCode,
    override val message: String? = null,
    override val throwable: Throwable? = null
) : LogException(errorCode, message ?: errorCode.defaultMessage, throwable)

open class ForbiddenException(
    override val errorCode: ErrorCode,
    override val message: String? = null,
    override val throwable: Throwable? = null
) : LogException(errorCode, message ?: errorCode.defaultMessage, throwable)

open class DataNotFoundException(
    override val errorCode: ErrorCode,
    override val message: String? = null,
    override val throwable: Throwable? = null
) : LogException(errorCode, message ?: errorCode.defaultMessage, throwable)
