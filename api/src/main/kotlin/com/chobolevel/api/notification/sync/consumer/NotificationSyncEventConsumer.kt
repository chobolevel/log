package com.chobolevel.api.notification.sync.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import com.chobolevel.api.notification.sse.SseEventName
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.api.notification.sync.dto.NotificationSyncEventMessage
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class NotificationSyncEventConsumer(
    private val notificationSyncEventRepository: NotificationSyncEventRepository,
    private val sseEmitterRegistry: SseEmitterRegistry,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도]
    // notifications row(source of truth)는 NotificationOutboxPublisher가 이미 동기로 저장했다.
    // 이 컨슈머는 그 사실을 "지금 연결돼 있는 클라이언트에게 실시간으로 전달"하는 것만 담당한다 — DB를
    // 다시 쓰지 않고 메시지에 이미 실려온 값을 그대로 SSE로 push하기만 하면 된다.
    //
    // FAILED는 다른 outbox(좋아요/조회수/팔로우)와 달리 재처리 대상이 아니다 — 관찰용 상태로만 남긴다.
    // 이 컨슈머는 "지금 이 순간" 실시간 전달하는 게 목적이라, 나중에 관리자가 재처리해도 이미 지나간
    // 순간을 되살릴 수 없고 오히려 뜬금없는 시점에 알림이 튀어나오는 부작용만 생긴다. notifications row는
    // 이미 저장돼 있어 유실이 아니므로 @RetryableTopic/DLQ/재처리 API를 두지 않는다.
    @KafkaListener(topics = [KafkaTopicConfiguration.NOTIFICATION_SYNC_EVENTS])
    @Transactional
    fun consume(message: NotificationSyncEventMessage) {
        val event: NotificationSyncEvent = notificationSyncEventRepository.findByIdOrNull(message.eventId) ?: return

        runCatching {
            sseEmitterRegistry.find(userId = message.userId)?.send(
                SseEmitter.event()
                    .name(SseEventName.NOTIFICATION)
                    .data(
                        NotificationSsePayload(
                            type = message.type,
                            content = message.content,
                            link = message.link,
                        )
                    )
            )
        }.onFailure { e ->
            logger.warn("NotificationSyncEvent SSE push 실패 - eventId: ${message.eventId}, userId: ${message.userId}", e)
            event.markFailed()
            return
        }

        event.markProcessed()
    }
}
