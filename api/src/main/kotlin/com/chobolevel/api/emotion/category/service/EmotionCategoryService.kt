package com.chobolevel.api.emotion.category.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.emotion.category.converter.EmotionCategoryConverter
import com.chobolevel.api.emotion.category.dto.CreateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse
import com.chobolevel.api.emotion.category.dto.SearchEmotionCategoryRequest
import com.chobolevel.api.emotion.category.dto.UpdateEmotionCategoryRequest
import com.chobolevel.api.emotion.category.updater.EmotionCategoryUpdater
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter
import com.chobolevel.domain.emotion.repository.EmotionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmotionCategoryService(
    private val emotionCategoryRepository: EmotionCategoryRepository,
    private val emotionRepository: EmotionRepository,
    private val emotionCategoryConverter: EmotionCategoryConverter,
    private val emotionCategoryUpdater: EmotionCategoryUpdater
) {

    @Transactional
    fun createEmotionCategory(request: CreateEmotionCategoryRequest): Long {
        val emotionCategory: EmotionCategory = emotionCategoryConverter.convert(request)
        return emotionCategoryRepository.save(emotionCategory).id!!
    }

    @Transactional(readOnly = true)
    fun searchEmotionCategories(request: SearchEmotionCategoryRequest): PagingResponse<EmotionCategoryResponse> {
        val queryFilter: EmotionCategoryQueryFilter = emotionCategoryConverter.convert(request = request)
        val paging = Paging(page = request.page, size = request.size)
        val orderTypes = request.orderTypes
        val emotionCategories: List<EmotionCategory> = emotionCategoryRepository.searchEmotionCategories(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = emotionCategoryRepository.searchEmotionCategoriesCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = emotionCategoryConverter.convert(entities = emotionCategories),
            totalCount = totalCount
        )
    }

    @Transactional
    fun updateEmotionCategory(emotionCategoryId: Long, request: UpdateEmotionCategoryRequest): Long {
        val emotionCategory: EmotionCategory = emotionCategoryRepository.findById(id = emotionCategoryId)
        emotionCategoryUpdater.markAsUpdate(request, emotionCategory)
        return emotionCategory.id!!
    }

    @Transactional
    fun deleteEmotionCategory(emotionCategoryId: Long): Boolean {
        val emotionCategory: EmotionCategory = emotionCategoryRepository.findById(emotionCategoryId)
        if (emotionRepository.existsByEmotionCategoryId(emotionCategoryId)) {
            throw PolicyViolationException(errorCode = ErrorCode.EMOTION_CATEGORY_IN_USE)
        }
        emotionCategory.delete()
        return true
    }
}
