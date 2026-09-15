package com.chobolevel.api.record.emotion.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.record.emotion.dto.UpdateRecordEmotionRequest
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.emotion.vo.RecordEmotionUpdateMask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordEmotionParameterValidatorTest : BehaviorSpec({

    val validator: RecordEmotionParameterValidator = RecordEmotionParameterValidator()

    given("INTENSITY updateMask를 검증할 때") {
        `when`("intensity 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordEmotionRequest = DummyRecord.toUpdateEmotionRequest()

                // when & then
                validator.validate(request)
            }
        }

        `when`("intensity 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordEmotionRequest = UpdateRecordEmotionRequest(
                    intensity = null,
                    updateMask = listOf(RecordEmotionUpdateMask.INTENSITY)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 감정 강도가 유효하지 않습니다."
            }
        }
    }
})
