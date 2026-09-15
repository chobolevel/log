package com.chobolevel.api.record.emotion.dto

import com.chobolevel.api.emotion.dto.EmotionResponse

data class RecordEmotionResponse(
    val id: Long,
    val emotion: EmotionResponse,
    val intensity: Int,
    val createdAt: Long,
    val updatedAt: Long
)
