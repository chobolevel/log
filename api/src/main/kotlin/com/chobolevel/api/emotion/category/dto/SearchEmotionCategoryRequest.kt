package com.chobolevel.api.emotion.category.dto

import com.chobolevel.domain.emotion.category.vo.EmotionCategoryOrderType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryType

data class SearchEmotionCategoryRequest(
    val name: String?,
    val type: EmotionCategoryType?,
    val page: Long = 1,
    val size: Long = 100,
    val orderTypes: List<EmotionCategoryOrderType> = emptyList()
)
