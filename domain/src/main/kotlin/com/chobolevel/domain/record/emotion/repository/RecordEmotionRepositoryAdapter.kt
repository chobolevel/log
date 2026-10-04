package com.chobolevel.domain.record.emotion.repository

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import com.chobolevel.domain.record.exception.RecordErrorCode
import org.springframework.stereotype.Component

@Component
class RecordEmotionRepositoryAdapter(
    private val recordEmotionJpaRepository: RecordEmotionJpaRepository
) : RecordEmotionRepository {

    override fun findById(id: Long): RecordEmotion {
        return recordEmotionJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw BusinessException(
            errorCode = RecordErrorCode.RECORD_EMOTION_NOT_FOUND
        )
    }

    override fun existsByEmotionId(emotionId: Long): Boolean {
        return recordEmotionJpaRepository.existsByEmotionIdAndIsDeletedFalse(emotionId)
    }
}
