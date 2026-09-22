package com.chobolevel.api.record.view.service

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.view.dto.RecordViewEventMessage
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.record.repository.RecordRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RecordViewService(
    private val recordRepository: RecordRepository,
    private val cacheProvider: CacheProvider,
    private val kafkaTemplate: KafkaTemplate<String, RecordViewEventMessage>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val DEDUP_WINDOW_HOURS = 24L
    }

    // [설계 의도]
    // RecordService가 "조회수를 어떻게 세고 반영하는지" 몰라도 되도록, 이 메서드는 dedup 판단과 이벤트 발행까지만 한다.
    // 실제 카운트 증가와 이력 영속화는 전부 RecordViewEventConsumer로 옮겨져 있다(같은 레이어 서비스 간 직접 의존 제거).
    //
    // 좋아요와 달리 Outbox 없이 dedup 게이트 통과 시 바로 Kafka에 발행한다.
    // 조회수는 유실을 허용하는 근사 지표이므로, 발행 실패는 로그만 남기고 요청 흐름을 막지 않는다.
    // userId가 있으면(로그인) guestId는 무시하고 userId로만 귀속시킨다 — RecordView는 둘 중 하나만 가져야 한다.
    // guestId는 필터가 항상 채워주지만(요청 attribute), 방어적으로 둘 다 없으면 집계 없이 조용히 스킵한다.
    fun recordView(recordId: Long, userId: Long?, guestId: String?): Boolean {
        val viewerKey: String = userId?.let { "user:$it" } ?: guestId?.let { "guest:$it" } ?: return false

        validateRecordExists(recordId = recordId)

        val dedupKey: String = CacheKeyPrefix.recordViewDedup(recordId = recordId, viewerKey = viewerKey)
        val isFirstViewInWindow: Boolean = cacheProvider.putIfAbsent(dedupKey, "1", DEDUP_WINDOW_HOURS, TimeUnit.HOURS)
        if (!isFirstViewInWindow) {
            return false
        }

        publishViewEvent(recordId = recordId, userId = userId, guestId = guestId.takeIf { userId == null })
        return true
    }

    private fun publishViewEvent(recordId: Long, userId: Long?, guestId: String?) {
        kafkaTemplate.send(
            KafkaTopicConfiguration.RECORD_VIEW_EVENTS,
            RecordViewEventMessage(recordId = recordId, userId = userId, guestId = guestId)
        ).whenComplete { _, exception ->
            if (exception != null) {
                logger.error("RecordViewEvent Kafka 발행 실패 - recordId: $recordId", exception)
            }
        }
    }

    private fun validateRecordExists(recordId: Long) {
        if (!recordRepository.existsById(recordId)) {
            throw DataNotFoundException(errorCode = ErrorCode.RECORD_NOT_FOUND)
        }
    }
}
