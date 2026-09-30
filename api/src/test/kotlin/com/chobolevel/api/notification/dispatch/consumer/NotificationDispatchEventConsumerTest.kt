package com.chobolevel.api.notification.dispatch.consumer

import com.chobolevel.api.common.dummy.DummyNotificationDispatchEvent
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class NotificationDispatchEventConsumerTest : BehaviorSpec({

    val notificationDispatchEventRepository: NotificationDispatchEventRepository = mockk()
    val sseEmitterRegistry: SseEmitterRegistry = mockk()
    val notificationSseDispatcher: NotificationSseDispatcher = mockk()
    val consumer = NotificationDispatchEventConsumer(
        notificationDispatchEventRepository = notificationDispatchEventRepository,
        sseEmitterRegistry = sseEmitterRegistry,
        notificationSseDispatcher = notificationSseDispatcher,
    )

    beforeEach { clearAllMocks() }

    given("알림 디스패치 이벤트를 소비할 때") {
        `when`("수신자가 SSE로 연결돼 있으면") {
            then("실제 전송은 Dispatcher에 위임하고, 이 자리에서 상태를 바꾸지 않는다") {
                // given
                val event: NotificationDispatchEvent = DummyNotificationDispatchEvent.toEntity()
                val message = NotificationDispatchEventMessage(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                val emitter: SseEmitter = mockk()
                every { notificationDispatchEventRepository.findByIdOrNull(message.eventId) } returns event
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
                event.status shouldBe NotificationDispatchEventStatus.PENDING
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
                val event: NotificationDispatchEvent = DummyNotificationDispatchEvent.toEntity()
                val message = NotificationDispatchEventMessage(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                every { notificationDispatchEventRepository.findByIdOrNull(message.eventId) } returns event
                every { sseEmitterRegistry.find(userId = message.userId) } returns null

                // when
                consumer.consume(message)

                // then
                event.status shouldBe NotificationDispatchEventStatus.PROCESSED
                verify(exactly = 0) { notificationSseDispatcher.dispatch(any(), any(), any(), any()) }
            }
        }

        `when`("이벤트 조회 중 예상치 못한 예외가 발생하면") {
            then("예외를 전파하지 않고 로그만 남긴다") {
                // given
                val message = NotificationDispatchEventMessage(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                every { notificationDispatchEventRepository.findByIdOrNull(message.eventId) } throws RuntimeException("DB 블립")

                // when & then (예외 없이 종료되면 성공)
                consumer.consume(message)
                verify(exactly = 0) { notificationSseDispatcher.dispatch(any(), any(), any(), any()) }
            }
        }
    }
})
