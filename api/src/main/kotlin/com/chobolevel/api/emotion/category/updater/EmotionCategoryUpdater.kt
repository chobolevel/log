package com.chobolevel.api.emotion.category.updater

import com.chobolevel.api.emotion.category.dto.UpdateEmotionCategoryRequest
import com.chobolevel.domain.emotion.category.entity.EmotionCategory
import com.chobolevel.domain.emotion.category.vo.EmotionCategoryUpdateMask
import org.springframework.stereotype.Component

@Component
class EmotionCategoryUpdater {

    fun markAsUpdate(request: UpdateEmotionCategoryRequest, entity: EmotionCategory): EmotionCategory {
        request.updateMask.forEach {
            when (it) {
                EmotionCategoryUpdateMask.NAME -> entity.updateName(request.name!!)
                EmotionCategoryUpdateMask.TYPE -> entity.updateType(request.type!!)
                EmotionCategoryUpdateMask.ORDER -> entity.updateOrder(request.order!!)
            }
        }
        return entity
    }
}
