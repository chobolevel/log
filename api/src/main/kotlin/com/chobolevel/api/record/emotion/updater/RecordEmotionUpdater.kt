package com.chobolevel.api.record.emotion.updater

import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.domain.record.emotion.entity.RecordEmotion
import com.chobolevel.domain.record.emotion.vo.RecordEmotionUpdateMask
import org.springframework.stereotype.Component

@Component
class RecordEmotionUpdater {

    fun markAsUpdate(request: UpdateRecordEmotionRequest, entity: RecordEmotion): RecordEmotion {
        request.updateMask.forEach {
            when (it) {
                RecordEmotionUpdateMask.INTENSITY -> entity.changeIntensity(request.intensity!!)
            }
        }
        return entity
    }
}
