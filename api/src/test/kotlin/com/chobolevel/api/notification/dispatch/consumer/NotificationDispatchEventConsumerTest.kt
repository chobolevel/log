package com.chobolevel.api.notification.dispatch.consumer

import com.chobolevel.api.common.dummy.DummyNotificationDispatchEvent
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify

class NotificationDispatchEventConsumerTest : BehaviorSpec({

    val notificationSseDispatcher: NotificationSseDispatcher = mockk()
    val consumer: NotificationDispatchEventConsumer = NotificationDispatchEventConsumer(
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
                    path = DummyNotificationDispatchEvent.PATH,
                )
                val payload = NotificationSsePayload(
                    type = message.type,
                    content = message.content,
                    path = message.path,
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
    }
})
