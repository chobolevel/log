package com.chobolevel.api.emotion.updater

import com.chobolevel.api.emotion.dto.UpdateEmotionRequest
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.repository.EmotionCategoryRepository
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.vo.EmotionUpdateMask
import org.springframework.stereotype.Component

@Component
class EmotionUpdater(
    private val emotionCategoryRepository: EmotionCategoryRepository
) {

    fun markAsUpdate(request: UpdateEmotionRequest, entity: Emotion): Emotion {
        request.updateMask.forEach {
            when (it) {
                EmotionUpdateMask.EMOTION_CATEGORY -> {
                    val emotionCategory: EmotionCategory = emotionCategoryRepository.findById(request.emotionCategoryId!!)
                    entity.updateEmotionCategory(emotionCategory)
                }
                EmotionUpdateMask.NAME -> entity.updateName(request.name!!)
                EmotionUpdateMask.ORDER -> entity.updateOrder(request.order!!)
            }
        }
        return entity
    }
}
