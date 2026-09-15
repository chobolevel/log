package com.chobolevel.api.emotion.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.emotion.converter.EmotionConverter
import com.chobolevel.api.emotion.dto.CreateEmotionRequest
import com.chobolevel.api.emotion.dto.EmotionResponse
import com.chobolevel.api.emotion.dto.SearchEmotionRequest
import com.chobolevel.api.emotion.dto.UpdateEmotionRequest
import com.chobolevel.api.emotion.updater.EmotionUpdater
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter
import com.chobolevel.domain.record.emotion.repository.RecordEmotionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmotionService(
    private val emotionRepository: EmotionRepository,
    private val emotionCategoryRepository: EmotionCategoryRepository,
    private val recordEmotionRepository: RecordEmotionRepository,
    private val emotionConverter: EmotionConverter,
    private val emotionUpdater: EmotionUpdater
) {

    @Transactional
    fun createEmotion(request: CreateEmotionRequest): Long {
        val emotionCategory: EmotionCategory = emotionCategoryRepository.findById(request.emotionCategoryId)
        val emotion: Emotion = Emotion.create(
            emotionCategory = emotionCategory,
            name = request.name,
            order = request.order
        )
        return emotionRepository.save(emotion).id!!
    }

    @Transactional(readOnly = true)
    fun searchEmotions(request: SearchEmotionRequest): PagingResponse<EmotionResponse> {
        val queryFilter: EmotionQueryFilter = emotionConverter.convert(request = request)
        val paging = Paging(page = request.page, size = request.size)
        val orderTypes = request.orderTypes
        val emotions: List<Emotion> = emotionRepository.searchEmotions(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = emotionRepository.searchEmotionsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = emotionConverter.convert(entities = emotions),
            totalCount = totalCount
        )
    }

    @Transactional
    fun updateEmotion(emotionId: Long, request: UpdateEmotionRequest): Long {
        val emotion: Emotion = emotionRepository.findById(id = emotionId)
        emotionUpdater.markAsUpdate(request, emotion)
        return emotion.id!!
    }

    @Transactional
    fun deleteEmotion(emotionId: Long): Boolean {
        val emotion: Emotion = emotionRepository.findById(emotionId)
        if (recordEmotionRepository.existsByEmotionId(emotionId)) {
            throw PolicyViolationException(errorCode = ErrorCode.EMOTION_IN_USE)
        }
        emotion.delete()
        return true
    }
}
