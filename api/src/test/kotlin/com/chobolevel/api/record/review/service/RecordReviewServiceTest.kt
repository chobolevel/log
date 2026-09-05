package com.chobolevel.api.record.review.service

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.repository.RecordReviewRepository
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

class RecordReviewServiceTest : BehaviorSpec({

    val recordReviewRepository: RecordReviewRepository = mockk()
    val recordBusinessValidator: RecordBusinessValidator = mockk()
    val recordReviewService: RecordReviewService = RecordReviewService(
        recordReviewRepository = recordReviewRepository,
        recordBusinessValidator = recordBusinessValidator
    )

    beforeEach { clearAllMocks() }

    given("기록 리뷰를 수정할 때") {
        `when`("유효한 요청이 들어오면") {
            then("수정된 기록 리뷰의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val reviewId: Long = DummyRecord.REVIEW_ID
                val newRating: BigDecimal = BigDecimal("3.0")
                val request: UpdateRecordReviewRequest = UpdateRecordReviewRequest(
                    rating = newRating,
                    updateMask = listOf(RecordReviewUpdateMask.RATING)
                )
                val record: com.chobolevel.domain.record.entity.Record = DummyRecord.toEntityWithReview()
                val review: RecordReview = record.recordReview!!
                every { recordReviewRepository.findById(reviewId) } returns review
                justRun { recordBusinessValidator.validateWriter(userId, review.record) }

                // when
                val result: Long = recordReviewService.updateRecordReview(
                    userId = userId,
                    reviewId = reviewId,
                    request = request
                )

                // then
                result shouldBe DummyRecord.REVIEW_ID
                review.rating shouldBe newRating
                verify { recordBusinessValidator.validateWriter(userId, review.record) }
            }
        }
    }

    given("기록 리뷰를 삭제할 때") {
        `when`("유효한 요청이 들어오면") {
            then("true를 반환하고 기록 리뷰는 삭제 처리된다") {
                // given
                val userId: Long = DummyUser.ID
                val reviewId: Long = DummyRecord.REVIEW_ID
                val record: com.chobolevel.domain.record.entity.Record = DummyRecord.toEntityWithReview()
                val review: RecordReview = record.recordReview!!
                every { recordReviewRepository.findById(reviewId) } returns review
                justRun { recordBusinessValidator.validateWriter(userId, review.record) }

                // when
                val result: Boolean = recordReviewService.deleteRecordReview(
                    userId = userId,
                    reviewId = reviewId
                )

                // then
                result shouldBe true
                review.isDeleted shouldBe true
                verify { recordBusinessValidator.validateWriter(userId, review.record) }
            }
        }
    }
})
