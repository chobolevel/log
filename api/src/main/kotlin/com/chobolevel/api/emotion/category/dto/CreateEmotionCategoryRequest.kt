package com.chobolevel.api.emotion.category.dto

import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateEmotionCategoryRequest(
    @field:NotEmpty(message = "감정 카테고리 이름은 필수 값입니다.")
    val name: String,
    @field:NotNull(message = "감정 카테고리 유형은 필수 값입니다.")
    val type: EmotionCategoryType,
    @field:NotNull(message = "감정 카테고리 순서는 필수 값입니다.")
    val order: Int
)
