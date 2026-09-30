package com.chobolevel.api.notification.sync.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.SseEmitterRegistry
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
    private val notificationSseDispatcher: NotificationSseDispatcher,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도]
    // notifications row(source of truth)는 NotificationOutboxPublisher가 이미 동기로 저장했다.
    // 이 컨슈머는 그 사실을 "지금 연결돼 있는 클라이언트에게 실시간으로 전달"하는 것만 담당한다 — DB를
    // 다시 쓰지 않고 메시지에 이미 실려온 값을 그대로 SSE로 push하기만 하면 된다.
    //
    // 실제 emitter.send()는 이 스레드(Kafka poll 스레드)에서 직접 하지 않고 NotificationSseDispatcher에
    // 위임한다 — 느린 클라이언트로의 전송이 블로킹되면 이 토픽 전체(다른 유저 몫까지)가 밀리기 때문.
    // 연결된 emitter가 없으면 보낼 게 없으니 그 자리에서 바로 PROCESSED로 표시한다.
    //
    // FAILED는 다른 outbox(좋아요/조회수/팔로우)와 달리 재처리 대상이 아니다 — 관찰용 상태로만 남긴다.
    // 이 컨슈머는 "지금 이 순간" 실시간 전달하는 게 목적이라, 나중에 관리자가 재처리해도 이미 지나간
    // 순간을 되살릴 수 없고 오히려 뜬금없는 시점에 알림이 튀어나오는 부작용만 생긴다. notifications row는
    // 이미 저장돼 있어 유실이 아니므로 @RetryableTopic/DLQ/재처리 API를 두지 않는다.
    // send() 실패가 아닌, findByIdOrNull() 같은 이 앞단에서 터지는 일시적 인프라 예외(DB 블립 등)를 그대로
    // 던지면 @RetryableTopic이 없어 Spring Kafka 기본 블로킹 재시도에 걸리고, 그동안 이 토픽 전체(다른 유저
    // 몫까지)가 지연된다. 재시도해서 얻을 이득도 없으므로 로그만 남기고 다음 메시지로 넘어간다.
    @KafkaListener(topics = [KafkaTopicConfiguration.NOTIFICATION_SYNC_EVENTS])
    @Transactional
    fun consume(message: NotificationSyncEventMessage) {
        try {
            val event: NotificationSyncEvent = notificationSyncEventRepository.findByIdOrNull(message.eventId) ?: return
            val emitter: SseEmitter? = sseEmitterRegistry.find(userId = message.userId)

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
            logger.error("NotificationSyncEvent 처리 중 예상치 못한 예외 발생 - eventId: ${message.eventId}", e)
        }
    }
}
