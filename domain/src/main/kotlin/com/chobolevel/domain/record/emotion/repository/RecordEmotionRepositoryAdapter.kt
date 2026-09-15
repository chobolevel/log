package com.chobolevel.domain.record.emotion.repository

import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import org.springframework.stereotype.Component

@Component
class RecordEmotionRepositoryAdapter(
    private val recordEmotionJpaRepository: RecordEmotionJpaRepository
) : RecordEmotionRepository {

    override fun findById(id: Long): RecordEmotion {
        return recordEmotionJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.RECORD_EMOTION_NOT_FOUND
        )
    }

    override fun existsByEmotionId(emotionId: Long): Boolean {
        return recordEmotionJpaRepository.existsByEmotionIdAndIsDeletedFalse(emotionId)
    }
}
