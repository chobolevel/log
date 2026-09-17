package com.chobolevel.api.record.view.consumer

import com.chobolevel.api.common.dummy.DummyRecordView
import com.chobolevel.api.record.view.dto.RecordViewEventMessage
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class RecordViewEventConsumerTest : BehaviorSpec({

    val recordViewRepository: RecordViewRepository = mockk()
    val consumer: RecordViewEventConsumer = RecordViewEventConsumer(
        recordViewRepository = recordViewRepository,
    )

    beforeEach { clearAllMocks() }

    given("조회 이벤트를 소비할 때") {
        `when`("회원의 조회 이벤트이면") {
            then("userId로 조회 이력을 저장한다") {
                // given
                val message: RecordViewEventMessage = DummyRecordView.toEventMessage(userId = DummyRecordView.USER_ID, guestId = null)
                val recordViewSlot: CapturingSlot<RecordView> = slot()
                every { recordViewRepository.save(capture(recordViewSlot)) } answers { firstArg() }

                // when
                consumer.consume(message = message)

                // then
                verify { recordViewRepository.save(any()) }
                recordViewSlot.captured.recordId shouldBe message.recordId
                recordViewSlot.captured.userId shouldBe message.userId
                recordViewSlot.captured.guestId shouldBe null
            }
        }

        `when`("비회원의 조회 이벤트이면") {
            then("guestId로 조회 이력을 저장한다") {
                // given
                val message: RecordViewEventMessage = DummyRecordView.toEventMessage(userId = null, guestId = DummyRecordView.GUEST_ID)
                val recordViewSlot: CapturingSlot<RecordView> = slot()
                every { recordViewRepository.save(capture(recordViewSlot)) } answers { firstArg() }

                // when
                consumer.consume(message = message)

                // then
                recordViewSlot.captured.userId shouldBe null
                recordViewSlot.captured.guestId shouldBe message.guestId
            }
        }
    }
})
