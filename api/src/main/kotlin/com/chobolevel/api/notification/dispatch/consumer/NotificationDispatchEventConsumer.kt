package com.chobolevel.api.notification.dispatch.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationDispatchEventConsumer(
    private val notificationSseDispatcher: NotificationSseDispatcher,
) {

    // 누구에게 연결이 있는지(Registry 조회), 이벤트 상태를 어떻게 갱신할지는 전부
    // NotificationSseDispatcher 책임이다 — 이 컨슈머는 메시지를 payload로 변환해 넘기기만 한다.
    // dispatch()는 @Async라 호출 즉시 리턴하고, 그 안의 실패는 dispatch() 자체의 runCatching이 처리한다 —
    // 여기서 try/catch로 감싸봐야 async 스레드에서 나는 예외는 못 잡으므로 의미가 없어 두지 않는다.
    @KafkaListener(topics = [KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS])
    fun consume(message: NotificationDispatchEventMessage) {
        notificationSseDispatcher.dispatch(
            eventId = message.eventId,
            userId = message.userId,
            payload = NotificationSsePayload(
                type = message.type,
                content = message.content,
                path = message.path,
            ),
        )
    }
}
