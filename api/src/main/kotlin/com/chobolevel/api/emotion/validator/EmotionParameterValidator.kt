package com.chobolevel.api.emotion.validator

import com.chobolevel.api.emotion.dto.UpdateEmotionRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.emotion.vo.EmotionUpdateMask
import org.springframework.stereotype.Component

@Component
class EmotionParameterValidator {

    fun validate(request: UpdateEmotionRequest) {
        request.updateMask.forEach {
            when (it) {
                EmotionUpdateMask.EMOTION_CATEGORY -> {
                    if (request.emotionCategoryId == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 카테고리 아이디 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                EmotionUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 이름 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                EmotionUpdateMask.ORDER -> {
                    if (request.order == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 순서 파라미터가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
