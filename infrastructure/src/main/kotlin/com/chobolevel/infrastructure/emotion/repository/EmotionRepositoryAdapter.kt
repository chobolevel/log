package com.chobolevel.infrastructure.emotion.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.exception.EmotionErrorCode
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.emotion.vo.EmotionOrderType
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
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
        return emotionJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw BusinessException(
            errorCode = EmotionErrorCode.EMOTION_NOT_FOUND
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
        return emotionQuerydslRepository.searchByFilter(
            filter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
    }

    override fun searchEmotionsCount(queryFilter: EmotionQueryFilter): Long {
        return emotionQuerydslRepository.countByFilter(filter = queryFilter)
    }
}
