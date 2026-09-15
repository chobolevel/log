package com.chobolevel.api.emotion.converter

import com.chobolevel.api.emotion.category.converter.EmotionCategoryConverter
import com.chobolevel.api.emotion.dto.EmotionResponse
import com.chobolevel.api.emotion.dto.SearchEmotionRequest
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
import org.springframework.stereotype.Component

@Component
class EmotionConverter(
    private val emotionCategoryConverter: EmotionCategoryConverter
) {

    fun convert(request: SearchEmotionRequest): EmotionQueryFilter {
        return EmotionQueryFilter(
            emotionCategoryId = request.emotionCategoryId,
            name = request.name
        )
    }

    fun convert(entity: Emotion): EmotionResponse {
        return EmotionResponse(
            id = entity.id!!,
            emotionCategory = emotionCategoryConverter.convert(entity.emotionCategory),
            name = entity.name,
            order = entity.order,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Emotion>): List<EmotionResponse> {
        return entities.map { convert(it) }
    }
}
