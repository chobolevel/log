package com.chobolevel.domain.emotion.category.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.entity.QEmotionCategory.emotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryOrderType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class EmotionCategoryQuerydslRepository : QuerydslRepositorySupport(EmotionCategory::class.java) {

    fun searchByFilter(
        filter: EmotionCategoryQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionCategoryOrderType>
    ): List<EmotionCategory> {
        return from(emotionCategory)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByFilter(filter: EmotionCategoryQueryFilter): Long {
        return from(emotionCategory)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: EmotionCategoryQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.name?.let { emotionCategory.name.eq(it) },
            filter.type?.let { emotionCategory.type.eq(it) },
            emotionCategory.isDeleted.isFalse
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<EmotionCategoryOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                EmotionCategoryOrderType.ORDER_ASC -> emotionCategory.order.asc()
                EmotionCategoryOrderType.ORDER_DESC -> emotionCategory.order.desc()
            }
        }.toTypedArray()
    }
}
