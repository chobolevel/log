package com.chobolevel.api.emotion.category.converter

import com.chobolevel.api.emotion.category.dto.CreateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse
import com.chobolevel.api.emotion.category.dto.SearchEmotionCategoryRequest
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import org.springframework.stereotype.Component

@Component
class EmotionCategoryConverter {

    fun convert(request: CreateEmotionCategoryRequest): EmotionCategory {
        return EmotionCategory.create(
            name = request.name,
            type = request.type,
            order = request.order
        )
    }

    fun convert(request: SearchEmotionCategoryRequest): EmotionCategoryQueryFilter {
        return EmotionCategoryQueryFilter(
            name = request.name,
            type = request.type
        )
    }

    fun convert(entity: EmotionCategory): EmotionCategoryResponse {
        return EmotionCategoryResponse(
            id = entity.id!!,
            name = entity.name,
            type = entity.type,
            order = entity.order,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<EmotionCategory>): List<EmotionCategoryResponse> {
        return entities.map { convert(it) }
    }
}
