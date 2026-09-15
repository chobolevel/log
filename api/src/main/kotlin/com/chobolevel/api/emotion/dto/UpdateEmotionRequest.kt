package com.chobolevel.api.emotion.dto

import com.chobolevel.domain.emotion.vo.EmotionUpdateMask
import jakarta.validation.constraints.Size

data class UpdateEmotionRequest(
    val emotionCategoryId: Long?,
    val name: String?,
    val order: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<EmotionUpdateMask>
)
