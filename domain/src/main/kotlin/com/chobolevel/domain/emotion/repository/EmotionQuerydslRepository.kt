package com.chobolevel.domain.emotion.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.entity.QEmotion.emotion
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class EmotionQuerydslRepository : QuerydslRepositorySupport(Emotion::class.java) {

    fun searchByPredicates(
        predicates: Array<BooleanExpression>,
        paging: Paging,
        orderSpecifiers: Array<OrderSpecifier<*>>
    ): List<Emotion> {
        return from(emotion)
            .where(*predicates)
            .orderBy(*orderSpecifiers)
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByPredicates(predicates: Array<BooleanExpression>): Long {
        return from(emotion)
            .where(*predicates)
            .fetchCount()
    }
}
