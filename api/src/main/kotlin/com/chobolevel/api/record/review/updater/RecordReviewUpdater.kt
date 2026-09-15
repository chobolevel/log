package com.chobolevel.api.record.review.updater

import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import org.springframework.stereotype.Component

@Component
class RecordReviewUpdater {

    fun markAsUpdate(request: UpdateRecordReviewRequest, entity: RecordReview): RecordReview {
        request.updateMask.forEach {
            when (it) {
                RecordReviewUpdateMask.RATING -> entity.changeRating(request.rating!!)
            }
        }
        return entity
    }
}
