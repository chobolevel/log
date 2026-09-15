package com.chobolevel.api.emotion.dto

import com.chobolevel.domain.emotion.vo.EmotionOrderType

data class SearchEmotionRequest(
    val emotionCategoryId: Long?,
    val name: String?,
    val page: Long = 1,
    val size: Long = 100,
    val orderTypes: List<EmotionOrderType> = emptyList()
)
