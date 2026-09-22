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
    val cacheProvider: CacheProvider = mockk()
    val kafkaTemplate: KafkaTemplate<String, RecordViewEventMessage> = mockk()
    val service: RecordViewService = RecordViewService(
        recordRepository = recordRepository,
        cacheProvider = cacheProvider,
        kafkaTemplate = kafkaTemplate,
    )

    beforeEach { clearAllMocks() }

    given("기록을 조회할 때") {
        `when`("로그인 사용자가 처음 조회하면") {
            then("true를 반환하고 userId로만 이벤트를 발행한다") {
                // given
                val recordId: Long = DummyRecord.ID
                val userId: Long = DummyUser.ID
                val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId, "user:$userId")
                val messageSlot: CapturingSlot<RecordViewEventMessage> = slot()
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
                every {
                    kafkaTemplate.send(eq(KafkaTopicConfiguration.RECORD_VIEW_EVENTS), capture(messageSlot))
                } returns CompletableFuture.completedFuture(null)

                // when
                val result: Boolean = service.recordView(recordId = recordId, userId = userId, guestId = "guest-id")

                // then
                result shouldBe true
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
                val messageSlot: CapturingSlot<RecordViewEventMessage> = slot()
                every { recordRepository.existsById(recordId) } returns true
                every { cacheProvider.putIfAbsent(dedupKey, "1", 24L, TimeUnit.HOURS) } returns true
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
            then("false를 반환하고 이벤트 발행을 하지 않는다") {
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
    }
})
