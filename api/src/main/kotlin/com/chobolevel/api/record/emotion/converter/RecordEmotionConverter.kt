package com.chobolevel.api.record.emotion.converter

import com.chobolevel.api.emotion.converter.EmotionConverter
import com.chobolevel.api.record.emotion.dto.RecordEmotionResponse
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import org.springframework.stereotype.Component

@Component
class RecordEmotionConverter(
    private val emotionConverter: EmotionConverter
) {

    fun convert(entity: RecordEmotion): RecordEmotionResponse {
        return RecordEmotionResponse(
            id = entity.id!!,
            emotion = emotionConverter.convert(entity.emotion),
            intensity = entity.intensity,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }
}
