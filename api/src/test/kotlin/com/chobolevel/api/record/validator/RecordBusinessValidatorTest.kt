package com.chobolevel.api.record.validator

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class RecordBusinessValidatorTest : BehaviorSpec({

    val validator: RecordBusinessValidator = RecordBusinessValidator()

    given("기록 작성자 검증을 할 때") {
        `when`("요청자가 기록 작성자이면") {
            then("예외가 발생하지 않는다") {
                // given
                val record: Record = DummyRecord.toEntity()
                val userId: Long = DummyUser.ID

                // when & then
                validator.validateWriter(userId = userId, record = record)
            }
        }

        `when`("요청자가 기록 작성자가 아니면") {
            then("ForbiddenException이 발생한다") {
                // given
                val record: Record = DummyRecord.toEntity()
                val otherUserId: Long = DummyUser.ID + 1L

                // when & then
                shouldThrow<ForbiddenException> {
                    validator.validateWriter(userId = otherUserId, record = record)
                }
            }
        }

        `when`("record가 null이면") {
            then("ForbiddenException이 발생한다") {
                // given
                val userId: Long = DummyUser.ID

                // when & then
                shouldThrow<ForbiddenException> {
                    validator.validateWriter(userId = userId, record = null)
                }
            }
        }
    }
})
