package com.chobolevel.domain.emotion.exception

import com.chobolevel.domain.common.exception.ErrorCode

enum class EmotionErrorCode(override val defaultMessage: String) : ErrorCode {
    // EMOTION CATEGORY
    EMOTION_CATEGORY_NOT_FOUND("감정 카테고리를 찾을 수 없습니다."),
    EMOTION_CATEGORY_IN_USE("사용 중인 감정 카테고리는 삭제할 수 없습니다."),

    // EMOTION
    EMOTION_NOT_FOUND("감정을 찾을 수 없습니다."),
    EMOTION_IN_USE("사용 중인 감정은 삭제할 수 없습니다.")
}
