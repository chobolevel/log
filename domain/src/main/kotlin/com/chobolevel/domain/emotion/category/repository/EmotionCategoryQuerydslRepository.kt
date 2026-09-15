package com.chobolevel.domain.emotion.category.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.entity.QEmotionCategory.emotionCategory
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class EmotionCategoryQuerydslRepository : QuerydslRepositorySupport(EmotionCategory::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<EmotionCategory> {
        return from(emotionCategory)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(emotionCategory)
            .where(*predicates)
            .fetchCount()
    }
}
