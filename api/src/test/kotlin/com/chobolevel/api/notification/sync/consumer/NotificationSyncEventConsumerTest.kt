package com.chobolevel.api.notification.sync.consumer

import com.chobolevel.api.common.dummy.DummyNotificationSyncEvent
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
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

class NotificationSyncEventConsumerTest : BehaviorSpec({

    val notificationSyncEventRepository: NotificationSyncEventRepository = mockk()
    val sseEmitterRegistry: SseEmitterRegistry = mockk()
    val notificationSseDispatcher: NotificationSseDispatcher = mockk()
    val consumer = NotificationSyncEventConsumer(
        notificationSyncEventRepository = notificationSyncEventRepository,
        sseEmitterRegistry = sseEmitterRegistry,
        notificationSseDispatcher = notificationSseDispatcher,
    )

    beforeEach { clearAllMocks() }

    given("알림 동기화 이벤트를 소비할 때") {
        `when`("수신자가 SSE로 연결돼 있으면") {
            then("실제 전송은 Dispatcher에 위임하고, 이 자리에서 상태를 바꾸지 않는다") {
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
                justRun {
                    notificationSseDispatcher.dispatch(
                        eventId = message.eventId,
                        userId = message.userId,
                        emitter = emitter,
                        payload = NotificationSsePayload(
                            type = message.type,
                            content = message.content,
                            link = message.link,
                        ),
                    )
                }

                // when
                consumer.consume(message)

                // then (Dispatcher는 mock이라 실제로 상태를 바꾸지 않음 — 여기서 상태가 그대로인 것만 확인)
                event.status shouldBe NotificationSyncEventStatus.PENDING
                verify(exactly = 1) {
                    notificationSseDispatcher.dispatch(
                        eventId = message.eventId,
                        userId = message.userId,
                        emitter = emitter,
                        payload = NotificationSsePayload(
                            type = message.type,
                            content = message.content,
                            link = message.link,
                        ),
                    )
                }
            }
        }

        `when`("수신자가 연결돼 있지 않으면") {
            then("Dispatcher를 거치지 않고 바로 이벤트를 PROCESSED로 표시한다") {
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
                verify(exactly = 0) { notificationSseDispatcher.dispatch(any(), any(), any(), any()) }
            }
        }
    }
})
