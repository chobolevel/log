package com.chobolevel.api.record.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.record.vo.RecordUpdateMask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RecordParameterValidatorTest : BehaviorSpec({

    val validator: RecordParameterValidator = RecordParameterValidator()

    given("TYPE updateMask를 검증할 때") {
        `when`("type 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = RecordType.REVIEW,
                    title = null,
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("type 값이 없으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.TYPE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 유형이 유효하지 않습니다."
            }
        }
    }

    given("TITLE updateMask를 검증할 때") {
        `when`("title 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = DummyRecord.toUpdateRequest()

                // when & then
                validator.validate(request)
            }
        }

        `when`("title 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.TITLE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 제목이 유효하지 않습니다."
            }
        }

        `when`("title 값이 빈 문자열이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = "",
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.TITLE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 제목이 유효하지 않습니다."
            }
        }
    }

    given("CONTENT updateMask를 검증할 때") {
        `when`("content 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = "새 내용",
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.CONTENT)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("content 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.CONTENT)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 기록 내용이 유효하지 않습니다."
            }
        }
    }

    given("IS_PRIVATE updateMask를 검증할 때") {
        `when`("isPrivate 값이 있으면") {
            then("예외가 발생하지 않는다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = true,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.IS_PRIVATE)
                )

                // when & then
                validator.validate(request)
            }
        }

        `when`("isPrivate 값이 null이면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: UpdateRecordRequest = UpdateRecordRequest(
                    type = null,
                    title = null,
                    content = null,
                    isPrivate = null,
                    review = null,
                    updateMask = listOf(RecordUpdateMask.IS_PRIVATE)
                )

                // when & then
                val ex: InvalidParameterException = shouldThrow {
                    validator.validate(request)
                }
                ex.message shouldBe "변경할 공개 여부가 유효하지 않습니다."
            }
        }
    }
})
