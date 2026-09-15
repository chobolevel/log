package com.chobolevel.api.record.review.service

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.api.record.review.updater.RecordReviewUpdater
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.repository.RecordReviewRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class RecordReviewServiceTest : BehaviorSpec({

    val recordReviewRepository: RecordReviewRepository = mockk()
    val recordBusinessValidator: RecordBusinessValidator = mockk()
    val recordReviewUpdater: RecordReviewUpdater = mockk()
    val recordReviewService: RecordReviewService = RecordReviewService(
        recordReviewRepository = recordReviewRepository,
        recordBusinessValidator = recordBusinessValidator,
        recordReviewUpdater = recordReviewUpdater
    )

    beforeEach { clearAllMocks() }

    given("기록 리뷰를 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 기록 리뷰의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val reviewId: Long = DummyRecord.REVIEW_ID
                val request: UpdateRecordReviewRequest = DummyRecord.toUpdateReviewRequest()
                val record: com.chobolevel.domain.record.entity.Record = DummyRecord.toEntityWithReview()
                val review: RecordReview = record.recordReview!!
                every { recordReviewRepository.findById(reviewId) } returns review
                justRun { recordBusinessValidator.validateWriter(userId, review.record) }
                every { recordReviewUpdater.markAsUpdate(request, review) } returns review

                // when
                val result: Long = recordReviewService.updateRecordReview(
                    userId = userId,
                    reviewId = reviewId,
                    request = request
                )

                // then
                result shouldBe DummyRecord.REVIEW_ID
                verify { recordBusinessValidator.validateWriter(userId, review.record) }
                verify { recordReviewUpdater.markAsUpdate(request, review) }
            }
        }
    }
})
