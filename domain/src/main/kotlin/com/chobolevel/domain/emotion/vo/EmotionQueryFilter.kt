package com.chobolevel.domain.emotion.vo

import com.chobolevel.domain.emotion.entity.QEmotion.emotion
import com.querydsl.core.types.dsl.BooleanExpression

class EmotionQueryFilter(
    private val emotionCategoryId: Long?,
    private val name: String?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            emotionCategoryId?.let { emotion.emotionCategory.id.eq(it) },
            name?.let { emotion.name.eq(it) },
            emotion.isDeleted.isFalse
        ).toTypedArray()
    }
}
