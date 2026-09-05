package com.chobolevel.api.record.review.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordReviewParameterValidatorTest : BehaviorSpec({

    val validator: RecordReviewParameterValidator = RecordReviewParameterValidator()

    given("RATING updateMask를 검증할 때") {
        `when`("rating 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordReviewRequest = DummyRecord.toUpdateReviewRequest()

                // when & then
                validator.validate(request)
            }
        }

        `when`("rating 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordReviewRequest = UpdateRecordReviewRequest(
                    rating = null,
                    updateMask = listOf(RecordReviewUpdateMask.RATING)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 평점이 유효하지 않습니다."
            }
        }
    }
})
