package com.chobolevel.domain.emotion.category.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryOrderType
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryQueryFilter

interface EmotionCategoryRepository {

    fun save(emotionCategory: EmotionCategory): EmotionCategory

    fun findById(id: Long): EmotionCategory

    fun searchEmotionCategories(
        queryFilter: EmotionCategoryQueryFilter,
        paging: Paging,
        orderTypes: List<EmotionCategoryOrderType>
    ): List<EmotionCategory>

    fun searchEmotionCategoriesCount(queryFilter: EmotionCategoryQueryFilter): Long
}
