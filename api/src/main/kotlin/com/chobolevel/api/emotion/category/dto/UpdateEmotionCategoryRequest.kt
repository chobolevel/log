package com.chobolevel.api.emotion.category.dto

import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryUpdateMask
import jakarta.validation.constraints.Size

data class UpdateEmotionCategoryRequest(
    val name: String?,
    val type: EmotionCategoryType?,
    val order: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<EmotionCategoryUpdateMask>
)
