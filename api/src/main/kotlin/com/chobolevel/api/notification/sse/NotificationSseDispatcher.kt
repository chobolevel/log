package com.chobolevel.api.notification.sse

import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class NotificationSseDispatcher(
    private val notificationDispatchEventRepository: NotificationDispatchEventRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async("sseTaskExecutor")
    @Transactional
    fun dispatch(eventId: Long, userId: Long, emitter: SseEmitter, payload: NotificationSsePayload) {
        runCatching {
            emitter.send(
                SseEmitter.event()
                    .name(SseEventName.NOTIFICATION)
                    .data(payload)
            )
        }.onSuccess {
            notificationDispatchEventRepository.findByIdOrNull(eventId)?.markProcessed()
        }.onFailure { e ->
            logger.warn("NotificationDispatchEvent SSE push 실패 - eventId: $eventId, userId: $userId", e)
            runCatching { emitter.completeWithError(e) }
            notificationDispatchEventRepository.findByIdOrNull(eventId)?.markFailed()
        }
    }

    @Async("sseTaskExecutor")
    fun dispatchHeartbeat(userId: Long, emitter: SseEmitter) {
        runCatching {
            emitter.send(SseEmitter.event().comment("heartbeat"))
        }.onFailure { e ->
            logger.info("Notification SSE heartbeat 실패, 연결 종료 - userId: $userId")
            runCatching { emitter.completeWithError(e) }
        }
    }
}
