package com.chobolevel.api.record.view.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.view.dto.RecordViewEventMessage
import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RecordViewEventConsumer(
    private val recordViewRepository: RecordViewRepository,
    private val cacheProvider: CacheProvider,
) {

    // [설계 의도]
    // 좋아요와 달리 Outbox/DLQ 없이 Spring Kafka 기본 에러 핸들링(재시도 후 스킵)에 맡긴다.
    // 이력은 append-only 로그라 재처리로 중복 행이 들어와도 치명적이지 않아 멱등성 보장이 불필요하다.
    //
    // Redis 조회수 증가도 여기서 한다(RecordViewService에서 옮겨옴 — RecordService가 "조회수를 어떻게 세는지"
    // 몰라도 되도록). 재시도 시 이론적으로 이 increment가 중복 실행될 수 있지만, 조회수는 원래 유실/오차를
    // 허용하는 근사 지표라 감수한다. 기획/요구사항이 바뀌면 그때 read-repair 등으로 재검토한다.
    @KafkaListener(topics = [KafkaTopicConfiguration.RECORD_VIEW_EVENTS])
    @Transactional
    fun consume(message: RecordViewEventMessage) {
        increaseViewCount(recordId = message.recordId)
        recordViewRepository.save(
            RecordView.create(
                recordId = message.recordId,
                userId = message.userId,
                guestId = message.guestId,
            )
        )
    }

    // 콜드스타트: Redis에 카운터 키가 없으면 record_views 이력 COUNT(*)로 시드값 세팅.
    // 아직 이번 조회를 DB에 저장하기 전이라 COUNT(*)는 "이전" 값이다 — 그래서 increment 한 번으로 정확해진다.
    private fun increaseViewCount(recordId: Long) {
        val countKey: String = CacheKeyPrefix.recordViewCount(recordId)
        if (!cacheProvider.hasKey(countKey)) {
            val currentCount: Long = recordViewRepository.countByRecordId(recordId = recordId)
            cacheProvider.putIfAbsent(countKey, currentCount.toString())
        }
        cacheProvider.increment(countKey)
    }
}
