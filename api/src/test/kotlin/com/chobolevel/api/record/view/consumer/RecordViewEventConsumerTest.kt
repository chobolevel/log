package com.chobolevel.api.record.view.consumer

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecordView
import com.chobolevel.api.common.provider.CacheProvider
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
    val cacheProvider: CacheProvider = mockk()
    val consumer: RecordViewEventConsumer = RecordViewEventConsumer(
        recordViewRepository = recordViewRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("조회 이벤트를 소비할 때") {
        `when`("회원의 조회 이벤트이면") {
            then("userId로 조회 이력을 저장하고 Redis 조회수를 증가시킨다") {
                // given
                val message: RecordViewEventMessage = DummyRecordView.toEventMessage(userId = DummyRecordView.USER_ID, guestId = null)
                val countKey: String = CacheKeyPrefix.recordViewCount(message.recordId)
                val recordViewSlot: CapturingSlot<RecordView> = slot()
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.increment(countKey) } returns 1L
                every { recordViewRepository.save(capture(recordViewSlot)) } answers { firstArg() }

                // when
                consumer.consume(message = message)

                // then
                verify { recordViewRepository.save(any()) }
                verify { cacheProvider.increment(countKey) }
                verify(exactly = 0) { recordViewRepository.countByRecordId(any()) }
                recordViewSlot.captured.recordId shouldBe message.recordId
                recordViewSlot.captured.userId shouldBe message.userId
                recordViewSlot.captured.guestId shouldBe null
            }
        }

        `when`("비회원의 조회 이벤트이면") {
            then("guestId로 조회 이력을 저장하고 Redis 조회수를 증가시킨다") {
                // given
                val message: RecordViewEventMessage = DummyRecordView.toEventMessage(userId = null, guestId = DummyRecordView.GUEST_ID)
                val countKey: String = CacheKeyPrefix.recordViewCount(message.recordId)
                val recordViewSlot: CapturingSlot<RecordView> = slot()
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.increment(countKey) } returns 1L
                every { recordViewRepository.save(capture(recordViewSlot)) } answers { firstArg() }

                // when
                consumer.consume(message = message)

                // then
                recordViewSlot.captured.userId shouldBe null
                recordViewSlot.captured.guestId shouldBe message.guestId
            }
        }

        `when`("캐시에 조회수 카운터가 없으면 (cold start)") {
            then("DB의 조회 이력 수로 시드값을 세팅한 뒤 증가시킨다") {
                // given
                val message: RecordViewEventMessage = DummyRecordView.toEventMessage(userId = DummyRecordView.USER_ID, guestId = null)
                val countKey: String = CacheKeyPrefix.recordViewCount(message.recordId)
                every { cacheProvider.hasKey(countKey) } returns false
                every { recordViewRepository.countByRecordId(message.recordId) } returns 10L
                every { cacheProvider.putIfAbsent(countKey, "10") } returns true
                every { cacheProvider.increment(countKey) } returns 11L
                every { recordViewRepository.save(any()) } answers { firstArg() }

                // when
                consumer.consume(message = message)

                // then
                verify { recordViewRepository.countByRecordId(message.recordId) }
                verify { cacheProvider.putIfAbsent(countKey, "10") }
                verify { cacheProvider.increment(countKey) }
            }
        }
    }
})
