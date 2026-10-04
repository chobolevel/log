package com.chobolevel.domain.common.exception

enum class SystemErrorCode(override val defaultMessage: String) : ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버에서 에러가 발생하였습니다."),
    LOCK_ACQUISITION_FAILED("요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요."),
    EMAIL_SEND_FAILED("이메일 발송에 실패했습니다.")
}
