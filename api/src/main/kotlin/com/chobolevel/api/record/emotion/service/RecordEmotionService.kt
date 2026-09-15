package com.chobolevel.api.record.emotion.service

import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.api.record.emotion.updater.RecordEmotionUpdater
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordEmotionService(
    private val recordEmotionRepository: RecordEmotionRepository,
    private val recordBusinessValidator: RecordBusinessValidator,
    private val recordEmotionUpdater: RecordEmotionUpdater
) {

    @Transactional
    fun updateRecordEmotion(userId: Long, recordEmotionId: Long, request: UpdateRecordEmotionRequest): Long {
        val recordEmotion: RecordEmotion = recordEmotionRepository.findById(recordEmotionId)
        recordBusinessValidator.validateWriter(userId, recordEmotion.record)
        recordEmotionUpdater.markAsUpdate(request, recordEmotion)
        return recordEmotion.id!!
    }
}
