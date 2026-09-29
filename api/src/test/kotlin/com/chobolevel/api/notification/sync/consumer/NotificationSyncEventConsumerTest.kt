package com.chobolevel.api.notification.sync.consumer

import com.chobolevel.api.common.dummy.DummyNotificationSyncEvent
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import com.chobolevel.api.notification.sync.dto.NotificationSyncEventMessage
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import com.chobolevel.domain.notification.sync.vo.NotificationSyncEventStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

class NotificationSyncEventConsumerTest : BehaviorSpec({

    val notificationSyncEventRepository: NotificationSyncEventRepository = mockk()
    val sseEmitterRegistry: SseEmitterRegistry = mockk()
    val consumer = NotificationSyncEventConsumer(
        notificationSyncEventRepository = notificationSyncEventRepository,
        sseEmitterRegistry = sseEmitterRegistry,
    )

    beforeEach { clearAllMocks() }

    given("알림 동기화 이벤트를 소비할 때") {
        `when`("수신자가 SSE로 연결돼 있으면") {
            then("연결된 emitter로 push하고 이벤트를 PROCESSED로 표시한다") {
                // given
                val event: NotificationSyncEvent = DummyNotificationSyncEvent.toEntity()
                val message = NotificationSyncEventMessage(
                    eventId = DummyNotificationSyncEvent.ID,
                    userId = DummyNotificationSyncEvent.USER_ID,
                    type = DummyNotificationSyncEvent.TYPE,
                    content = DummyNotificationSyncEvent.CONTENT,
                    link = DummyNotificationSyncEvent.LINK,
                )
                val emitter: SseEmitter = mockk()
                every { notificationSyncEventRepository.findByIdOrNull(message.eventId) } returns event
                every { sseEmitterRegistry.find(userId = message.userId) } returns emitter
                justRun { emitter.send(any<SseEmitter.SseEventBuilder>()) }

                // when
                consumer.consume(message)

                // then
                event.status shouldBe NotificationSyncEventStatus.PROCESSED
                verify(exactly = 1) { emitter.send(any<SseEmitter.SseEventBuilder>()) }
            }
        }

        `when`("수신자가 연결돼 있지 않으면") {
            then("push 없이 이벤트를 PROCESSED로 표시한다") {
                // given
                val event: NotificationSyncEvent = DummyNotificationSyncEvent.toEntity()
                val message = NotificationSyncEventMessage(
                    eventId = DummyNotificationSyncEvent.ID,
                    userId = DummyNotificationSyncEvent.USER_ID,
                    type = DummyNotificationSyncEvent.TYPE,
                    content = DummyNotificationSyncEvent.CONTENT,
                    link = DummyNotificationSyncEvent.LINK,
                )
                every { notificationSyncEventRepository.findByIdOrNull(message.eventId) } returns event
                every { sseEmitterRegistry.find(userId = message.userId) } returns null

                // when
                consumer.consume(message)

                // then
                event.status shouldBe NotificationSyncEventStatus.PROCESSED
            }
        }

        `when`("push 도중 예외가 발생하면") {
            then("이벤트를 FAILED로 표시한다 (재처리 스케줄러/DLQ가 없어 이 상태는 관찰 목적으로만 남는다)") {
                // given
                val event: NotificationSyncEvent = DummyNotificationSyncEvent.toEntity()
                val message = NotificationSyncEventMessage(
                    eventId = DummyNotificationSyncEvent.ID,
                    userId = DummyNotificationSyncEvent.USER_ID,
                    type = DummyNotificationSyncEvent.TYPE,
                    content = DummyNotificationSyncEvent.CONTENT,
                    link = DummyNotificationSyncEvent.LINK,
                )
                val emitter: SseEmitter = mockk()
                every { notificationSyncEventRepository.findByIdOrNull(message.eventId) } returns event
                every { sseEmitterRegistry.find(userId = message.userId) } returns emitter
                every { emitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")

                // when
                consumer.consume(message)

                // then
                event.status shouldBe NotificationSyncEventStatus.FAILED
            }
        }
    }
})
