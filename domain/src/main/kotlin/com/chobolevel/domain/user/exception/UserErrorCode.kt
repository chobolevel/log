package com.chobolevel.domain.user.exception

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorType

enum class UserErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    // USER
    USER_NOT_FOUND(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다."),
    USER_PASSWORD_NOT_MATCHED(ErrorType.INVALID, "비밀번호가 일치하지 않습니다."),
    USER_PASSWORD_REUSING_NOT_ALLOWED(ErrorType.INVALID, "동일한 비밀번호를 사용할 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS(ErrorType.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_EMAIL_NOT_EXISTS(ErrorType.NOT_FOUND, "존재하지 않는 이메일입니다."),
    USER_NICKNAME_ALREADY_EXISTS(ErrorType.CONFLICT, "이미 존재하는 닉네임입니다."),
    EMAIL_VERIFICATION_CODE_NOT_MATCHED(ErrorType.INVALID, "이메일 확인 코드가 일치하지 않습니다."),
    RESET_USER_PASSWORD_CODE_NOT_EXISTS(ErrorType.INVALID, "비밀번호 초기화 코드가 없습니다."),

    // USER IMAGE
    USER_IMAGE_NOT_FOUND(ErrorType.NOT_FOUND, "회원 이미지를 찾을 수 없습니다."),

    // USER FOLLOW (ALREADY_EXISTS / NOT_FOUND 는 멱등 처리 단계에서 삭제 예정)
    USER_FOLLOW_ALREADY_EXISTS(ErrorType.CONFLICT, "이미 팔로우한 회원입니다."),
    USER_FOLLOW_NOT_FOUND(ErrorType.NOT_FOUND, "팔로우하지 않은 회원입니다."),
    USER_FOLLOW_SELF_NOT_ALLOWED(ErrorType.INVALID, "자기 자신은 팔로우할 수 없습니다."),

    // USER FOLLOW SYNC EVENT
    USER_FOLLOW_SYNC_EVENT_NOT_FOUND(ErrorType.NOT_FOUND, "팔로우 동기화 이벤트를 찾을 수 없습니다."),
    USER_FOLLOW_SYNC_EVENT_NOT_FAILED(ErrorType.CONFLICT, "실패한 이벤트만 재발행할 수 있습니다.")
}
