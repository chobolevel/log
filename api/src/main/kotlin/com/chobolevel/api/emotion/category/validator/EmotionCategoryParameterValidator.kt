package com.chobolevel.api.emotion.category.validator

import com.chobolevel.api.emotion.category.dto.UpdateEmotionCategoryRequest
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryUpdateMask
import org.springframework.stereotype.Component

@Component
class EmotionCategoryParameterValidator {

    fun validate(request: UpdateEmotionCategoryRequest) {
        request.updateMask.forEach {
            when (it) {
                EmotionCategoryUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw BusinessException(
                            errorCode = CommonErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 카테고리 이름 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                EmotionCategoryUpdateMask.TYPE -> {
                    if (request.type == null) {
                        throw BusinessException(
                            errorCode = CommonErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 카테고리 유형 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                EmotionCategoryUpdateMask.ORDER -> {
                    if (request.order == null) {
                        throw BusinessException(
                            errorCode = CommonErrorCode.INVALID_PARAMETER,
                            message = "변경할 감정 카테고리 순서 파라미터가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
