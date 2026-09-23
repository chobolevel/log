package com.chobolevel.api.record.view.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.util.ReflectionTestUtils

class RecordViewValidatorTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val validator: RecordViewValidator = RecordViewValidator(recordRepository = recordRepository)

    beforeEach { clearAllMocks() }

    given("기록 조회 가능 여부를 검증할 때") {
        `when`("공개 기록이면") {
            then("예외가 발생하지 않는다") {
                // given
                val record: Record = DummyRecord.toEntity()
                every { recordRepository.findById(DummyRecord.ID) } returns record

                // when & then
                validator.validateViewable(requesterId = null, recordId = DummyRecord.ID)
            }
        }

        `when`("비공개 기록이고 요청자가 작성자이면") {
            then("예외가 발생하지 않는다") {
                // given
                val userId: Long = DummyUser.ID
                val record: Record = DummyRecord.toEntity().also { ReflectionTestUtils.setField(it, "isPrivate", true) }
                every { recordRepository.findById(DummyRecord.ID) } returns record

                // when & then
                validator.validateViewable(requesterId = userId, recordId = DummyRecord.ID)
            }
        }

        `when`("비공개 기록이고 요청자가 작성자가 아니면") {
            then("ForbiddenException이 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1L
                val record: Record = DummyRecord.toEntity().also { ReflectionTestUtils.setField(it, "isPrivate", true) }
                every { recordRepository.findById(DummyRecord.ID) } returns record

                // when & then
                shouldThrow<ForbiddenException> {
                    validator.validateViewable(requesterId = otherUserId, recordId = DummyRecord.ID)
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
            }
        }
    }
})
