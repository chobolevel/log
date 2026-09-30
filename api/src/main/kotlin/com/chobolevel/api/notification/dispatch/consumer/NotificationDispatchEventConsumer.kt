package com.chobolevel.api.notification.dispatch.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationDispatchEventConsumer(
    private val notificationSseDispatcher: NotificationSseDispatcher,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // 누구에게 연결이 있는지(Registry 조회), 이벤트 상태를 어떻게 갱신할지는 전부
    // NotificationSseDispatcher 책임이다 — 이 컨슈머는 메시지를 payload로 변환해 넘기기만 한다.
    @KafkaListener(topics = [KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS])
    fun consume(message: NotificationDispatchEventMessage) {
        try {
            notificationSseDispatcher.dispatch(
                eventId = message.eventId,
                userId = message.userId,
                payload = NotificationSsePayload(
                    type = message.type,
                    content = message.content,
                    link = message.link,
                ),
            )
        } catch (e: Exception) {
            logger.error("NotificationDispatchEvent 처리 중 예상치 못한 예외 발생 - eventId: ${message.eventId}", e)
        }
    }
}
