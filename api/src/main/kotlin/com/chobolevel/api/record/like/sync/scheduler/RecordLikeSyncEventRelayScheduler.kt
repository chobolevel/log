package com.chobolevel.api.record.like.sync.scheduler

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventMessage
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RecordLikeSyncEventRelayScheduler(
    private val recordLikeSyncEventRepository: RecordLikeSyncEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, RecordLikeSyncEventMessage>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: Transactional Outbox Relay]
    //
    // like()/dislike() 와 같은 트랜잭션에서 INSERT된 PENDING 이벤트를 Kafka로 중계한다.
    // 각 이벤트는 독립적으로 처리되며, Kafka 발행 성공 시에만 PUBLISHED로 업데이트한다.
    // 발행 실패 시 PENDING 상태가 유지되어 다음 실행 주기에 자동으로 재시도된다.
    @Scheduled(fixedDelay = 5_000)
    fun relay() {
        val pendingEvents: List<RecordLikeSyncEvent> = recordLikeSyncEventRepository
            .findAllByStatus(RecordLikeSyncEventStatus.PENDING)

        if (pendingEvents.isEmpty()) return

        logger.info("RecordLikeSyncEvent relay 시작 - ${pendingEvents.size}개")

        pendingEvents.forEach { event ->
            runCatching {
                kafkaTemplate.send(
                    KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS,
                    event.id!!.toString(),
                    RecordLikeSyncEventMessage(
                        eventId = event.id!!,
                        recordId = event.recordId,
                        userId = event.userId,
                        action = event.action,
                    )
                ).get(5, TimeUnit.SECONDS)

                event.markPublished()
                recordLikeSyncEventRepository.save(event)
            }.onFailure { e ->
                logger.error("RecordLikeSyncEvent Kafka 발행 실패 - eventId: ${event.id}", e)
            }
        }

        logger.info("RecordLikeSyncEvent relay 완료")
    }
}
