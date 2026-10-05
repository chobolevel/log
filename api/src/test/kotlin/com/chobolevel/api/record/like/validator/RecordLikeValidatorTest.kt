package com.chobolevel.api.record.like.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.record.exception.RecordErrorCode
import com.chobolevel.domain.record.repository.RecordRepository
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk

class RecordLikeValidatorTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val validator: RecordLikeValidator = RecordLikeValidator(recordRepository = recordRepository)

    beforeEach { clearAllMocks() }

    given("좋아요 대상 기록의 존재를 검증할 때") {
        `when`("기록이 존재하면") {
            then("예외 없이 통과한다") {
                // given
                every { recordRepository.existsById(id = DummyRecord.ID) } returns true

                // when & then
                shouldNotThrow<BusinessException> { validator.validateRecordExists(recordId = DummyRecord.ID) }
            }
        }

        `when`("기록이 존재하지 않으면") {
            then("RECORD_NOT_FOUND BusinessException이 발생한다") {
                // given
                every { recordRepository.existsById(id = DummyRecord.ID) } returns false

                // when & then
                shouldThrow<BusinessException> {
                    validator.validateRecordExists(recordId = DummyRecord.ID)
                }.errorCode shouldBe RecordErrorCode.RECORD_NOT_FOUND
            }
        }
    }
})
