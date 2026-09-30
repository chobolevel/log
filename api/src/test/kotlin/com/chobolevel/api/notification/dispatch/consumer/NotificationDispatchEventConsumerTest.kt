package com.chobolevel.api.notification.dispatch.consumer

import com.chobolevel.api.common.dummy.DummyNotificationDispatchEvent
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class NotificationDispatchEventConsumerTest : BehaviorSpec({

    val notificationSseDispatcher: NotificationSseDispatcher = mockk()
    val consumer = NotificationDispatchEventConsumer(
        notificationSseDispatcher = notificationSseDispatcher,
    )

    beforeEach { clearAllMocks() }

    given("알림 디스패치 이벤트를 소비할 때") {
        `when`("정상적으로 처리되면") {
            then("메시지를 payload로 변환해 Dispatcher에 위임한다") {
                // given
                val message = NotificationDispatchEventMessage(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                val payload = NotificationSsePayload(
                    type = message.type,
                    content = message.content,
                    link = message.link,
                )
                justRun {
                    notificationSseDispatcher.dispatch(eventId = message.eventId, userId = message.userId, payload = payload)
                }

                // when
                consumer.consume(message)

                // then
                verify(exactly = 1) {
                    notificationSseDispatcher.dispatch(eventId = message.eventId, userId = message.userId, payload = payload)
                }
            }
        }

        `when`("Dispatcher 위임 중 예상치 못한 예외가 발생하면") {
            then("예외를 전파하지 않고 로그만 남긴다") {
                // given
                val message = NotificationDispatchEventMessage(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                every {
                    notificationSseDispatcher.dispatch(eventId = message.eventId, userId = message.userId, payload = any())
                } throws RuntimeException("스레드풀 거부")

                // when & then (예외 없이 종료되면 성공)
                consumer.consume(message)
            }
        }
    }
})
