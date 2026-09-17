package com.chobolevel.api.record.view.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.record.view.dto.RecordViewEventMessage
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RecordViewEventConsumer(
    private val recordViewRepository: RecordViewRepository,
) {

    // [설계 의도]
    // 좋아요와 달리 Outbox/DLQ 없이 Spring Kafka 기본 에러 핸들링(재시도 후 스킵)에 맡긴다.
    // 이력은 append-only 로그라 재처리로 중복 행이 들어와도 치명적이지 않아 멱등성 보장이 불필요하다.
    @KafkaListener(topics = [KafkaTopicConfiguration.RECORD_VIEW_EVENTS])
    @Transactional
    fun consume(message: RecordViewEventMessage) {
        recordViewRepository.save(
            RecordView.create(
                recordId = message.recordId,
                userId = message.userId,
                guestId = message.guestId,
            )
        )
    }
}
