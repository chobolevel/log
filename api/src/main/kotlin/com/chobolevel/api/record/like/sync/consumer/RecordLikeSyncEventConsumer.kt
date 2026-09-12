package com.chobolevel.api.record.like.sync.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventMessage
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RecordLikeSyncEventConsumer(
    private val recordRepository: RecordRepository,
    private val userRepository: UserRepository,
    private val recordLikeRepository: RecordLikeRepository,
    private val recordLikeSyncEventRepository: RecordLikeSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: Idempotent Consumer]
    //
    // LIKE: existsByRecordIdAndUserId 선확인으로 중복 INSERT 방지 (Kafka retry 시에도 안전)
    // DISLIKE: deleteByRecordIdAndUserId는 존재하지 않는 row 삭제 시 no-op이므로 멱등성 보장
    //
    // Redis 업데이트는 DB 커밋 후 best-effort로 수행된다.
    // Redis 실패 시 트랜잭션이 롤백되어 Kafka가 재시도하고, like()의 cold start 로직이 최종적으로 Redis를 복구한다.
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1_000, multiplier = 2.0),
        retryTopicSuffix = "-retry",
        dltTopicSuffix = "-dlq",
    )
    @KafkaListener(topics = [KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS])
    @Transactional
    fun consume(message: RecordLikeSyncEventMessage) {
        when (message.action) {
            RecordLikeSyncEventAction.LIKE -> handleLike(message)
            RecordLikeSyncEventAction.DISLIKE -> handleDislike(message)
        }
        recordLikeSyncEventRepository.findByIdOrNull(message.eventId)?.markProcessed()
    }

    @DltHandler
    @Transactional
    fun handleDlt(message: RecordLikeSyncEventMessage) {
        logger.error("RecordLikeSyncEvent DLQ 도달 - 수동 처리 필요: eventId=${message.eventId}, recordId=${message.recordId}, userId=${message.userId}, action=${message.action}")
        recordLikeSyncEventRepository.findByIdOrNull(message.eventId)?.markFailed()
    }

    private fun handleLike(message: RecordLikeSyncEventMessage) {
        if (recordLikeRepository.existsByRecordIdAndUserId(message.recordId, message.userId)) {
            return
        }
        val record = recordRepository.findById(message.recordId)
        val user = userRepository.findById(message.userId)
        recordLikeRepository.save(RecordLike.create(record = record, user = user))

        cacheProvider.addToSet(CacheKeyPrefix.recordLikes(message.recordId), message.userId.toString())
    }

    private fun handleDislike(message: RecordLikeSyncEventMessage) {
        recordLikeRepository.deleteByRecordIdAndUserId(
            recordId = message.recordId,
            userId = message.userId,
        )
        cacheProvider.removeFromSet(CacheKeyPrefix.recordLikes(message.recordId), message.userId.toString())
    }
}
