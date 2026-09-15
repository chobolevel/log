package com.chobolevel.domain.emotion.category.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.entity.QEmotionCategory.emotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryOrderType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import com.querydsl.core.types.OrderSpecifier
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
        return emotionCategoryJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.EMOTION_CATEGORY_NOT_FOUND
        )
    }

    override fun searchEmotionCategories(
        queryFilter: EmotionCategoryQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionCategoryOrderType>
    ): List<EmotionCategory> {
        return emotionCategoryQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchEmotionCategoriesCount(queryFilter: EmotionCategoryQueryFilter): Long {
        return emotionCategoryQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<EmotionCategoryOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                EmotionCategoryOrderType.ORDER_ASC -> emotionCategory.order.asc()
                EmotionCategoryOrderType.ORDER_DESC -> emotionCategory.order.desc()
            }
        }.toTypedArray()
    }
}
