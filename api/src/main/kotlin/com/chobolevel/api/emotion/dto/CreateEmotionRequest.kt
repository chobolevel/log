package com.chobolevel.api.emotion.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateEmotionRequest(
    @field:NotNull(message = "감정 카테고리 아이디는 필수 값입니다.")
    val emotionCategoryId: Long,
    @field:NotEmpty(message = "감정 이름은 필수 값입니다.")
    val name: String,
    @field:NotNull(message = "감정 순서는 필수 값입니다.")
    val order: Int
)
