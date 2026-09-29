package com.chobolevel.api.notification.sync.scheduler

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.sync.dto.NotificationSyncEventMessage
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import com.chobolevel.domain.notification.sync.vo.NotificationSyncEventStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class NotificationSyncEventRelayScheduler(
    private val notificationSyncEventRepository: NotificationSyncEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, NotificationSyncEventMessage>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val RELAY_CHUNK_SIZE = 500L
    }

    // [설계 의도: Transactional Outbox Relay]
    //
    // NotificationOutboxPublisher.publish()와 같은 트랜잭션에서 INSERT된 PENDING 이벤트를 Kafka로 중계한다.
    // 각 이벤트는 독립적으로 처리되며, Kafka 발행 성공 시에만 PUBLISHED로 업데이트한다.
    // 발행 실패 시 PENDING 상태가 유지되어 다음 실행 주기에 자동으로 재시도된다.
    //
    // 한 번에 최대 RELAY_CHUNK_SIZE개(오래된 순)만 가져온다(RecordLikeSyncEventRelayScheduler와 동일 이유).
    //
    // 메시지 키는 event.id가 아닌 userId(수신자)를 사용한다.
    // 파티션이 1개인 지금은 동작에 차이가 없지만, 파티션을 늘릴 때 같은 수신자의 알림이
    // 항상 같은 파티션으로 라우팅되어 SSE 전달 순서가 보장된다.
    @Scheduled(fixedDelay = 5_000)
    fun relay() {
        val pendingEvents: List<NotificationSyncEvent> = notificationSyncEventRepository
            .findAllByStatusOrderByIdAsc(NotificationSyncEventStatus.PENDING, RELAY_CHUNK_SIZE)

        if (pendingEvents.isEmpty()) return

        logger.info("NotificationSyncEvent relay 시작 - ${pendingEvents.size}개")

        pendingEvents.forEach { event ->
            runCatching {
                kafkaTemplate.send(
                    KafkaTopicConfiguration.NOTIFICATION_SYNC_EVENTS,
                    event.userId.toString(),
                    NotificationSyncEventMessage(
                        eventId = event.id!!,
                        userId = event.userId,
                        type = event.type,
                        content = event.content,
                        link = event.link,
                    )
                ).get(5, TimeUnit.SECONDS)

                event.markPublished()
                notificationSyncEventRepository.save(event)
            }.onFailure { e ->
                logger.error("NotificationSyncEvent Kafka 발행 실패 - eventId: ${event.id}", e)
            }
        }

        logger.info("NotificationSyncEvent relay 완료")
    }
}
