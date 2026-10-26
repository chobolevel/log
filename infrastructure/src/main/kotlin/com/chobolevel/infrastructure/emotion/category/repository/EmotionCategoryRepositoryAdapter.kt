package com.chobolevel.infrastructure.emotion.category.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryOrderType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import com.chobolevel.domain.emotion.exception.EmotionErrorCode
import org.springframework.stereotype.Component

@Component
class EmotionCategoryRepositoryAdapter(
    private val emotionCategoryJpaRepository: EmotionCategoryJpaRepository,
    private val emotionCategoryQuerydslRepository: EmotionCategoryQuerydslRepository
) : EmotionCategoryRepository {

    override fun save(emotionCategory: EmotionCategory): EmotionCategory {
        return emotionCategoryJpaRepository.save(emotionCategory)
    }

    override fun findById(id: Long): EmotionCategory {
        return emotionCategoryJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw BusinessException(
            errorCode = EmotionErrorCode.EMOTION_CATEGORY_NOT_FOUND
        )
    }

    override fun searchEmotionCategories(
        queryFilter: EmotionCategoryQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionCategoryOrderType>
    ): List<EmotionCategory> {
        return emotionCategoryQuerydslRepository.searchByFilter(
            filter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
    }

    override fun searchEmotionCategoriesCount(queryFilter: EmotionCategoryQueryFilter): Long {
        return emotionCategoryQuerydslRepository.countByFilter(filter = queryFilter)
    }
}
