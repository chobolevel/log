package com.chobolevel.domain.emotion.category.vo

import com.chobolevel.domain.emotion.category.entity.QEmotionCategory.emotionCategory
import com.querydsl.core.types.dsl.BooleanExpression

class EmotionCategoryQueryFilter(
    private val name: String?,
    private val type: EmotionCategoryType?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            name?.let { emotionCategory.name.eq(it) },
            type?.let { emotionCategory.type.eq(it) },
            emotionCategory.isDeleted.isFalse
        ).toTypedArray()
    }
}
