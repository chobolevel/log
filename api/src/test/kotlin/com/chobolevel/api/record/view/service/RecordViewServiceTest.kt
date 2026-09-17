package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyRecordView
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.view.dto.RecordViewEventMessage
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class RecordViewServiceTest : BehaviorSpec({

    val recordRepository: RecordRepository = mockk()
    val recordViewRepository: RecordViewRepository = mockk()
    val cacheProvider: CacheProvider = mockk()
    val kafkaTemplate: KafkaTemplate<String, RecordViewEventMessage> = mockk()
    val service: RecordViewService = RecordViewService(
        recordRepository = recordRepository,
        recordViewRepository = recordViewRepository,
        cacheProvider = cacheProvider,
        kafkaTemplate = kafkaTemplate,
    )

    beforeEach { clearAllMocks() }

    given("기록을 조회할 때") {
        `when`("로그인 사용자가 처음 조회하면") {
            then("true를 반환하고 카운터를 올리고 userId로만 이벤트를 발행한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                val messageSlot: CapturingSlot<RecordViewEventMessage> = slot()
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.increment(countKey) } returns 1L
                every {
                    kafkaTemplate.send(eq(KafkaTopicConfiguration.RECORD_VIEW_EVENTS), capture(messageSlot))
                } returns CompletableFuture.completedFuture(null)

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = userId, guestId = "guest-id")

                // then
                result shouldBe true
                verify { cacheProvider.increment(countKey) }
                messageSlot.captured.userId shouldBe userId
                messageSlot.captured.guestId shouldBe null
            }
        }

        `when`("비회원이 처음 조회하면") {
            then("true를 반환하고 guestId로 이벤트를 발행한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val guestId: String = DummyRecordView.GUEST_ID
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "guest:$guestId")
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                val messageSlot: CapturingSlot<RecordViewEventMessage> = slot()
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every { cacheProvider.hasKey(countKey) } returns true
                every { cacheProvider.increment(countKey) } returns 1L
                every {
                    kafkaTemplate.send(eq(KafkaTopicConfiguration.RECORD_VIEW_EVENTS), capture(messageSlot))
                } returns CompletableFuture.completedFuture(null)

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = null, guestId = guestId)

                // then
                result shouldBe true
                messageSlot.captured.userId shouldBe null
                messageSlot.captured.guestId shouldBe guestId
            }
        }

        `when`("같은 방문자가 24시간 이내에 재조회하면") {
            then("false를 반환하고 카운터도 이벤트 발행도 하지 않는다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns false

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = userId, guestId = "guest-id")

                // then
                result shouldBe false
                verify(exactly = 0) { cacheProvider.increment(any()) }
                verify(exactly = 0) { kafkaTemplate.send(any<String>(), any()) }
            }
        }

        `when`("userId와 guestId가 모두 없으면") {
            then("false를 반환하고 기록 존재 여부조차 확인하지 않는다") {
                // given
                val recordId: Long = DummyRecord.ID

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = null, guestId = null)

                // then
                result shouldBe false
                verify(exactly = 0) { recordRepository.existsById(any()) }
            }
        }

        `when`("존재하지 않는 기록이면") {
            then("DataNotFoundException이 발생한다") {
                // given
                val recordId: Long = DummyRecord.ID
                every { recordRepository.existsById(recordId) } returns false

                // when & then
                shouldThrow<DataNotFoundException> {
                    service.recordView(recordId = recordId, userId = DummyUser.ID, guestId = null)
                }
            }
        }

        `when`("캐시에 카운터가 없으면 (cold start)") {
            then("DB의 조회 이력 수로 시드값을 세팅한 뒤 증가시킨다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every { cacheProvider.hasKey(countKey) } returns false
                every { recordViewRepository.countByRecordId(recordId) } returns 10L
                every { cacheProvider.putIfAbsent(countKey, "10") } returns true
                every { cacheProvider.increment(countKey) } returns 11L
                every {
                    kafkaTemplate.send(eq(KafkaTopicConfiguration.RECORD_VIEW_EVENTS), any())
                } returns CompletableFuture.completedFuture(null)

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = userId, guestId = null)

                // then
                result shouldBe true
                verify { recordViewRepository.countByRecordId(recordId) }
                verify { cacheProvider.putIfAbsent(countKey, "10") }
                verify { cacheProvider.increment(countKey) }
            }
        }
    }
})
