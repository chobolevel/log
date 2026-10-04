package com.chobolevel.domain.subject.exception

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorType

enum class SubjectErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    // SUBJECT
    SUBJECT_NOT_FOUND(ErrorType.NOT_FOUND, "주제를 찾을 수 없습니다."),

    // SUBJECT IMAGE
    SUBJECT_IMAGE_NOT_FOUND(ErrorType.NOT_FOUND, "주제 이미지를 찾을 수 없습니다.")
}
