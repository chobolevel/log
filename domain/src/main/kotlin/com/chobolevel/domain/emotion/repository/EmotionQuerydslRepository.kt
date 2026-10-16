package com.chobolevel.domain.emotion.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.entity.QEmotion.emotion
import com.chobolevel.domain.emotion.vo.EmotionOrderType
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class EmotionQuerydslRepository : QuerydslRepositorySupport(Emotion::class.java) {

    fun searchByFilter(
        filter: EmotionQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionOrderType>
    ): List<Emotion> {
        return from(emotion)
            .where(*predicatesOf(filter))
            .orderBy(*orderSpecifiersOf(orderTypes))
            .offset(paging.offset)
            .limit(paging.limit)
            .fetch()
    }

    fun countByFilter(filter: EmotionQueryFilter): Long {
        return from(emotion)
            .where(*predicatesOf(filter))
            .fetchCount()
    }

    // 필터를 QueryDSL 조건식으로 번역한다. BooleanExpression은 이 클래스 밖으로 나가지 않는다.
    private fun predicatesOf(filter: EmotionQueryFilter): Array<BooleanExpression> {
        return listOfNotNull(
            filter.emotionCategoryId?.let { emotion.emotionCategory.id.eq(it) },
            filter.name?.let { emotion.name.eq(it) },
            emotion.isDeleted.isFalse
        ).toTypedArray()
    }

    private fun orderSpecifiersOf(orderTypes: List<EmotionOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                EmotionOrderType.ORDER_ASC -> emotion.order.asc()
                EmotionOrderType.ORDER_DESC -> emotion.order.desc()
            }
        }.toTypedArray()
    }
}
