package com.chobolevel.domain.common.exception

enum class CommonErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    INVALID_PARAMETER(ErrorType.INVALID, "파라미터가 유효하지 않습니다."),
    INVALID_REQUEST_FORMAT(ErrorType.INVALID, "요청 형식이 올바르지 않습니다."),
    DUPLICATE_REQUEST(ErrorType.CONFLICT, "이미 처리된 요청입니다."),
    ACCESS_DENIED(ErrorType.FORBIDDEN, "접근 권한이 없습니다.")
}
