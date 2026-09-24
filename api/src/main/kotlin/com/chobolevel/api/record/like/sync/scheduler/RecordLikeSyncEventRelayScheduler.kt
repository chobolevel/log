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

    companion object {
        private const val RELAY_CHUNK_SIZE = 500L
    }

    // [설계 의도: Transactional Outbox Relay]
    //
    // like()/dislike() 와 같은 트랜잭션에서 INSERT된 PENDING 이벤트를 Kafka로 중계한다.
    // 각 이벤트는 독립적으로 처리되며, Kafka 발행 성공 시에만 PUBLISHED로 업데이트한다.
    // 발행 실패 시 PENDING 상태가 유지되어 다음 실행 주기에 자동으로 재시도된다.
    //
    // 한 번에 최대 RELAY_CHUNK_SIZE개(오래된 순)만 가져온다 — Kafka 장애 등으로 PENDING이
    // 쌓여도 한 tick이 무제한 풀 스캔/로드를 하지 않도록 상한을 둔다. 남은 백로그는
    // 5초 뒤 다음 tick이 이어서 처리한다.
    //
    // 메시지 키는 event.id가 아닌 recordId를 사용한다.
    // 파티션이 1개인 지금은 동작에 차이가 없지만, 파티션을 늘릴 때 키가 recordId여야
    // 같은 기록에 대한 이벤트가 항상 같은 파티션으로 라우팅되어 순서가 보장된다.
    @Scheduled(fixedDelay = 5_000)
    fun relay() {
        val pendingEvents: List<RecordLikeSyncEvent> = recordLikeSyncEventRepository
            .findAllByStatusOrderByIdAsc(RecordLikeSyncEventStatus.PENDING, RELAY_CHUNK_SIZE)

        if (pendingEvents.isEmpty()) return

        logger.info("RecordLikeSyncEvent relay 시작 - ${pendingEvents.size}개")

        pendingEvents.forEach { event ->
            runCatching {
                kafkaTemplate.send(
                    KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS,
                    event.recordId.toString(),
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
