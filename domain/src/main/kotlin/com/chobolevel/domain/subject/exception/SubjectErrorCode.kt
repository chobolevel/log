package com.chobolevel.domain.subject.exception

import com.chobolevel.domain.common.exception.ErrorCode

enum class SubjectErrorCode(override val defaultMessage: String) : ErrorCode {
    // SUBJECT
    SUBJECT_NOT_FOUND("주제를 찾을 수 없습니다."),

    // SUBJECT IMAGE
    SUBJECT_IMAGE_NOT_FOUND("주제 이미지를 찾을 수 없습니다.")
}
