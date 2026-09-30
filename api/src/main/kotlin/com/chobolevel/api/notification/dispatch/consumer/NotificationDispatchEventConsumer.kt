package com.chobolevel.api.notification.dispatch.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class NotificationDispatchEventConsumer(
    private val notificationDispatchEventRepository: NotificationDispatchEventRepository,
    private val sseEmitterRegistry: SseEmitterRegistry,
    private val notificationSseDispatcher: NotificationSseDispatcher,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS])
    @Transactional
    fun consume(message: NotificationDispatchEventMessage) {
        try {
            val event: NotificationDispatchEvent = notificationDispatchEventRepository.findByIdOrNull(message.eventId) ?: return
            val emitter: SseEmitter? = sseEmitterRegistry.find(userId = message.userId)

            // 전송할 Emitter가 없는 경우 종료
            if (emitter == null) {
                event.markProcessed()
                return
            }

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
        } catch (e: Exception) {
            logger.error("NotificationDispatchEvent 처리 중 예상치 못한 예외 발생 - eventId: ${message.eventId}", e)
        }
    }
}
