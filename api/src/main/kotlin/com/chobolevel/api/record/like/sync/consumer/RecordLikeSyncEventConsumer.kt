package com.chobolevel.api.record.like.sync.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventMessage
import com.chobolevel.domain.common.exception.LogException
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
class RecordLikeSyncEventConsumer(
    private val recordLikeRepository: RecordLikeRepository,
    private val recordLikeSyncEventRepository: RecordLikeSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: read-repair 기반 Idempotent Consumer]
    //
    // record_likes에 대한 쓰기(INSERT/DELETE)는 이미 API 요청 트랜잭션에서 동기로 끝나 있다.
    // 이 Consumer는 DB를 다시 쓰지 않고, DB(source of truth)를 조회해 그 값으로 Redis를 덮어쓰기만 한다.
    // 델타(증감) 재적용이 아니라 절대값 overwrite이기 때문에, Kafka가 같은 이벤트를 몇 번을 재전달해도
    // 결과가 항상 동일하다 — LIKE/DISLIKE로 분기할 필요 자체가 없다.
    //
    // LogException 계열(데이터 정합성 문제)은 재시도해도 결과가 달라지지 않으므로 즉시 DLQ로 보낸다.
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1_000, multiplier = 2.0),
        retryTopicSuffix = "-retry",
        dltTopicSuffix = "-dlq",
        exclude = [LogException::class],
        traversingCauses = "true",
    )
    @KafkaListener(topics = [KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS])
    @Transactional
    fun consume(message: RecordLikeSyncEventMessage) {
        reconcileCache(recordId = message.recordId, userId = message.userId)
        recordLikeSyncEventRepository.findByIdOrNull(message.eventId)?.markProcessed()
    }

    @DltHandler
    @Transactional
    fun handleDlt(message: RecordLikeSyncEventMessage) {
        logger.error("RecordLikeSyncEvent DLQ 도달 - 관리자 재발행 필요: eventId=${message.eventId}, recordId=${message.recordId}, userId=${message.userId}, action=${message.action}")
        recordLikeSyncEventRepository.findByIdOrNull(message.eventId)?.markFailed()
    }

    private fun reconcileCache(recordId: Long, userId: Long) {
        val likeCount: Long = recordLikeRepository.countByRecordId(recordId)
        val liked: Boolean = recordLikeRepository.existsByRecordIdAndUserId(recordId = recordId, userId = userId)

        cacheProvider.put(
            CacheKeyPrefix.recordLikeCount(recordId),
            likeCount.toString(),
            CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES,
        )
        cacheProvider.put(
            CacheKeyPrefix.recordLike(recordId = recordId, userId = userId),
            if (liked) "1" else "0",
            CacheKeyPrefix.RECORD_LIKE_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES,
        )
    }
}
