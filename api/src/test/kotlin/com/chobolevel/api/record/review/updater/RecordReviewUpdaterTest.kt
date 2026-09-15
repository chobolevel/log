package com.chobolevel.api.record.review.updater

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class RecordReviewUpdaterTest : BehaviorSpec({

    val updater: RecordReviewUpdater = RecordReviewUpdater()

    given("기록 리뷰 수정 요청으로 엔티티를 업데이트할 때") {
        `when`("RATING 마스크이면") {
            then("rating이 변경된 RecordReview를 반환한다") {
                val record: Record = DummyRecord.toEntityWithReview()
                val review: RecordReview = record.recordReview!!
                val newRating: BigDecimal = BigDecimal("3.0")
                val request: UpdateRecordReviewRequest = UpdateRecordReviewRequest(
                    rating = newRating,
                    updateMask = listOf(RecordReviewUpdateMask.RATING)
                )

                val result: RecordReview = updater.markAsUpdate(request, review)

                result.rating shouldBe newRating
            }
        }
    }
})
