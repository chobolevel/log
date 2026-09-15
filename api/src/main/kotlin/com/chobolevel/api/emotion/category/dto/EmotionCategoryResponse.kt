package com.chobolevel.api.emotion.category.dto

import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType

data class EmotionCategoryResponse(
    val id: Long,
    val name: String,
    val type: EmotionCategoryType,
    val order: Int,
    val createdAt: Long,
    val updatedAt: Long
)
