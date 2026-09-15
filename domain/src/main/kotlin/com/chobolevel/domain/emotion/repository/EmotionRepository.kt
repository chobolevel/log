package com.chobolevel.domain.emotion.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.vo.EmotionOrderType
import com.chobolevel.domain.emotion.vo.EmotionQueryFilter

interface EmotionRepository {

    fun save(emotion: Emotion): Emotion

    fun findById(id: Long): Emotion

    fun existsByEmotionCategoryId(emotionCategoryId: Long): Boolean

    fun searchEmotions(
        queryFilter: EmotionQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionOrderType>
    ): List<Emotion>

    fun searchEmotionsCount(queryFilter: EmotionQueryFilter): Long
}
