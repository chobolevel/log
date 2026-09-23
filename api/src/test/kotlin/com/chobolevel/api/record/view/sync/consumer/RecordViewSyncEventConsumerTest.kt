package com.chobolevel.api.record.view.sync.consumer

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecordViewSyncEvent
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventMessage
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class RecordViewSyncEventConsumerTest : BehaviorSpec({

    val recordViewRepository: RecordViewRepository = mockk()
    val recordViewSyncEventRepository: RecordViewSyncEventRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val consumer: RecordViewSyncEventConsumer = RecordViewSyncEventConsumer(
        recordViewRepository = recordViewRepository,
        recordViewSyncEventRepository = recordViewSyncEventRepository,
        cacheProvider = cacheProvider,
    )

    beforeEach { clearAllMocks() }

    given("조회 동기화 이벤트를 소비할 때") {
        `when`("정상적으로 처리되면") {
            then("DB 조회 이력 수로 Redis 조회수를 절대값으로 덮어쓰고 이벤트를 PROCESSED로 전환한다") {
                // given
                val message = RecordViewSyncEventMessage(
                    eventId = DummyRecordViewSyncEvent.ID,
                    recordId = DummyRecordViewSyncEvent.RECORD_ID,
                )
                val countKey: String = CacheKeyPrefix.recordViewCount(message.recordId)
                val event: RecordViewSyncEvent = DummyRecordViewSyncEvent.toEntity()
                every { recordViewRepository.countByRecordId(message.recordId) } returns 5L
                every { cacheProvider.put(countKey, "5") } returns Unit
                every { recordViewSyncEventRepository.findByIdOrNull(message.eventId) } returns event

                // when
                consumer.consume(message = message)

                // then
                verify { cacheProvider.put(countKey, "5") }
                event.status shouldBe RecordViewSyncEventStatus.PROCESSED
            }
        }
    }

    given("조회 동기화 이벤트가 DLQ에 도달했을 때") {
        `when`("handleDlt가 호출되면") {
            then("이벤트를 FAILED로 전환한다") {
                // given
                val message = RecordViewSyncEventMessage(
                    eventId = DummyRecordViewSyncEvent.ID,
                    recordId = DummyRecordViewSyncEvent.RECORD_ID,
                )
                val event: RecordViewSyncEvent = DummyRecordViewSyncEvent.toEntity()
                every { recordViewSyncEventRepository.findByIdOrNull(message.eventId) } returns event

                // when
                consumer.handleDlt(message = message)

                // then
                event.status shouldBe RecordViewSyncEventStatus.FAILED
            }
        }
    }
})
