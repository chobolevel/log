package com.chobolevel.domain.emotion.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.entity.QEmotion.emotion
import com.chobolevel.domain.emotion.vo.EmotionOrderType
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.stereotype.Component

@Component
class EmotionRepositoryAdapter(
    private val emotionJpaRepository: EmotionJpaRepository,
    private val emotionQuerydslRepository: EmotionQuerydslRepository
) : EmotionRepository {

    override fun save(emotion: Emotion): Emotion {
        return emotionJpaRepository.save(emotion)
    }

    override fun findById(id: Long): Emotion {
        return emotionJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.EMOTION_NOT_FOUND
        )
    }

    override fun existsByEmotionCategoryId(emotionCategoryId: Long): Boolean {
        return emotionJpaRepository.existsByEmotionCategoryIdAndIsDeletedFalse(emotionCategoryId)
    }

    override fun searchEmotions(
        queryFilter: EmotionQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionOrderType>
    ): List<Emotion> {
        return emotionQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchEmotionsCount(queryFilter: EmotionQueryFilter): Long {
        return emotionQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<EmotionOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                EmotionOrderType.ORDER_ASC -> emotion.order.asc()
                EmotionOrderType.ORDER_DESC -> emotion.order.desc()
            }
        }.toTypedArray()
    }
}
