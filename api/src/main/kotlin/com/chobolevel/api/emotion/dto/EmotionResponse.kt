package com.chobolevel.api.emotion.dto

import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse

data class EmotionResponse(
    val id: Long,
    val emotionCategory: EmotionCategoryResponse,
    val name: String,
    val order: Int,
    val createdAt: Long,
    val updatedAt: Long
)
