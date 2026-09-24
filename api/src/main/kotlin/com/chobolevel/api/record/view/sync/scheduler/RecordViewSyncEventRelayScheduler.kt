package com.chobolevel.api.record.view.sync.scheduler

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventMessage
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RecordViewSyncEventRelayScheduler(
    private val recordViewSyncEventRepository: RecordViewSyncEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, RecordViewSyncEventMessage>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val RELAY_CHUNK_SIZE = 500L
    }

    // [설계 의도: Transactional Outbox Relay]
    // recordView()와 같은 트랜잭션에서 INSERT된 PENDING 이벤트를 Kafka로 중계한다.
    // 각 이벤트는 독립적으로 처리되며, Kafka 발행 성공 시에만 PUBLISHED로 업데이트한다.
    // 발행 실패 시 PENDING 상태가 유지되어 다음 실행 주기에 자동으로 재시도된다.
    //
    // 한 번에 최대 RELAY_CHUNK_SIZE개(오래된 순)만 가져온다 — 조회 이벤트는 좋아요/팔로우보다
    // 발생 빈도가 훨씬 높아 무제한 조회 시 백로그가 쌓이면 위험이 더 크다(RecordLikeSyncEventRelayScheduler와 동일 이유).
    //
    // 메시지 키는 event.id가 아닌 recordId를 사용한다 — 파티션을 늘릴 때 같은 기록에 대한
    // 이벤트가 항상 같은 파티션으로 라우팅되어 순서가 보장되도록 (RecordLikeSyncEventRelayScheduler와 동일 이유).
    @Scheduled(fixedDelay = 5_000)
    fun relay() {
        val pendingEvents: List<RecordViewSyncEvent> = recordViewSyncEventRepository
            .findAllByStatusOrderByIdAsc(RecordViewSyncEventStatus.PENDING, RELAY_CHUNK_SIZE)

        if (pendingEvents.isEmpty()) return

        logger.info("RecordViewSyncEvent relay 시작 - ${pendingEvents.size}개")

        pendingEvents.forEach { event ->
            runCatching {
                kafkaTemplate.send(
                    KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS,
                    event.recordId.toString(),
                    RecordViewSyncEventMessage(
                        eventId = event.id!!,
                        recordId = event.recordId,
                    )
                ).get(5, TimeUnit.SECONDS)

                event.markPublished()
                recordViewSyncEventRepository.save(event)
            }.onFailure { e ->
                logger.error("RecordViewSyncEvent Kafka 발행 실패 - eventId: ${event.id}", e)
            }
        }

        logger.info("RecordViewSyncEvent relay 완료")
    }
}
