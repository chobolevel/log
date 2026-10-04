package com.chobolevel.domain.emotion.exception

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorType

enum class EmotionErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    // EMOTION CATEGORY
    EMOTION_CATEGORY_NOT_FOUND(ErrorType.NOT_FOUND, "감정 카테고리를 찾을 수 없습니다."),
    EMOTION_CATEGORY_IN_USE(ErrorType.CONFLICT, "사용 중인 감정 카테고리는 삭제할 수 없습니다."),

    // EMOTION
    EMOTION_NOT_FOUND(ErrorType.NOT_FOUND, "감정을 찾을 수 없습니다."),
    EMOTION_IN_USE(ErrorType.CONFLICT, "사용 중인 감정은 삭제할 수 없습니다.")
}
