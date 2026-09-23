package com.chobolevel.api.record.view.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class RecordViewValidatorTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val recordBusinessValidator: RecordBusinessValidator = mockk()
    val validator: RecordViewValidator = RecordViewValidator(
        recordRepository = recordRepository,
        recordBusinessValidator = recordBusinessValidator,
    )

    beforeEach { clearAllMocks() }

    given("기록 조회 가능 여부를 검증할 때") {
        `when`("기록이 존재하고 조회 가능하면") {
            then("RecordBusinessValidator에 위임하고 예외가 발생하지 않는다") {
                // given
                val record: Record = DummyRecord.toEntity()
                every { recordRepository.findById(DummyRecord.ID) } returns record
                justRun { recordBusinessValidator.validateReadable(requesterId = DummyUser.ID, record = record) }

                // when
                validator.validateViewable(requesterId = DummyUser.ID, recordId = DummyRecord.ID)

                // then
                verify { recordBusinessValidator.validateReadable(requesterId = DummyUser.ID, record = record) }
            }
        }

        `when`("RecordBusinessValidator가 ForbiddenException을 던지면") {
            then("그대로 전파된다") {
                // given
                val record: Record = DummyRecord.toEntity()
                every { recordRepository.findById(DummyRecord.ID) } returns record
                every {
                    recordBusinessValidator.validateReadable(requesterId = null, record = record)
                } throws ForbiddenException(errorCode = ErrorCode.PRIVATE_RECORD)

                // when & then
                shouldThrow<ForbiddenException> {
                    validator.validateViewable(requesterId = null, recordId = DummyRecord.ID)
                }
            }
        }

        `when`("존재하지 않는 기록이면") {
            then("DataNotFoundException이 발생한다") {
                // given
                every { recordRepository.findById(DummyRecord.ID) } throws DataNotFoundException(
                    errorCode = ErrorCode.RECORD_NOT_FOUND
                )

                // when & then
                shouldThrow<DataNotFoundException> {
                    validator.validateViewable(requesterId = null, recordId = DummyRecord.ID)
                }
                verify(exactly = 0) { recordBusinessValidator.validateReadable(any(), any()) }
            }
        }
    }
})
