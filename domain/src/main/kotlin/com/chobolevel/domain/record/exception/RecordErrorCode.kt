package com.chobolevel.domain.record.exception

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorType

enum class RecordErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    // RECORD
    RECORD_NOT_FOUND(ErrorType.NOT_FOUND, "기록을 찾을 수 없습니다."),
    RESTRICTED_TO_RECORD_WRITER(ErrorType.FORBIDDEN, "기록 작성자만 접근 가능합니다."),
    PRIVATE_RECORD(ErrorType.FORBIDDEN, "비밀 기록입니다."),

    // RECORD REVIEW
    RECORD_REVIEW_NOT_FOUND(ErrorType.NOT_FOUND, "기록 리뷰를 찾을 수 없습니다."),

    // RECORD LIKE (ALREADY_EXISTS / NOT_FOUND 는 멱등 처리 단계에서 삭제 예정)
    RECORD_LIKE_ALREADY_EXISTS(ErrorType.CONFLICT, "이미 좋아요한 기록입니다."),
    RECORD_LIKE_NOT_FOUND(ErrorType.NOT_FOUND, "좋아요하지 않은 기록입니다."),

    // RECORD LIKE SYNC EVENT
    RECORD_LIKE_SYNC_EVENT_NOT_FOUND(ErrorType.NOT_FOUND, "좋아요 동기화 이벤트를 찾을 수 없습니다."),
    RECORD_LIKE_SYNC_EVENT_NOT_FAILED(ErrorType.CONFLICT, "실패한 이벤트만 재발행할 수 있습니다."),

    // RECORD VIEW SYNC EVENT
    RECORD_VIEW_SYNC_EVENT_NOT_FOUND(ErrorType.NOT_FOUND, "조회 동기화 이벤트를 찾을 수 없습니다."),
    RECORD_VIEW_SYNC_EVENT_NOT_FAILED(ErrorType.CONFLICT, "실패한 이벤트만 재발행할 수 있습니다."),

    // RECORD EMOTION
    RECORD_EMOTION_NOT_FOUND(ErrorType.NOT_FOUND, "기록 감정을 찾을 수 없습니다.")
}
