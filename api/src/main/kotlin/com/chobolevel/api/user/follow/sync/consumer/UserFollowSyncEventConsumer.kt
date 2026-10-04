package com.chobolevel.api.user.follow.sync.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventMessage
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserFollowSyncEventConsumer(
    private val userFollowRepository: UserFollowRepository,
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: read-repair 기반 Idempotent Consumer]
    //
    // user_follows에 대한 쓰기(INSERT/DELETE)는 이미 API 요청 트랜잭션에서 동기로 끝나 있다.
    // 이 Consumer는 DB를 다시 쓰지 않고, DB(source of truth)를 조회해 그 값으로 Redis를 덮어쓰기만 한다.
    // 델타(증감) 재적용이 아니라 절대값 overwrite이기 때문에, Kafka가 같은 이벤트를 몇 번을 재전달해도
    // 결과가 항상 동일하다 — FOLLOW/UNFOLLOW로 분기할 필요 자체가 없다 (RecordLikeSyncEventConsumer와 동일 패턴).
    //
    // BusinessException 계열(회원 삭제 등 데이터 정합성 문제)은 재시도해도 결과가 달라지지 않으므로
    // 재시도 없이 즉시 DLQ로 보낸다.
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1_000, multiplier = 2.0),
        retryTopicSuffix = "-retry",
        dltTopicSuffix = "-dlq",
        exclude = [BusinessException::class],
        traversingCauses = "true",
    )
    @KafkaListener(topics = [KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS])
    @Transactional
    fun consume(message: UserFollowSyncEventMessage) {
        reconcileCache(followerUserId = message.followerUserId, followingUserId = message.followingUserId)
        userFollowSyncEventRepository.findByIdOrNull(message.eventId)?.markProcessed()
    }

    @DltHandler
    @Transactional
    fun handleDlt(message: UserFollowSyncEventMessage) {
        logger.error(
            "UserFollowSyncEvent DLQ 도달 - 관리자 재발행 필요: eventId=${message.eventId}, " +
                "followerUserId=${message.followerUserId}, followingUserId=${message.followingUserId}, action=${message.action}"
        )
        userFollowSyncEventRepository.findByIdOrNull(message.eventId)?.markFailed()
    }

    private fun reconcileCache(followerUserId: Long, followingUserId: Long) {
        val followingCount: Long = userFollowRepository.countByFollowerUserId(followerUserId = followerUserId)
        val followerCount: Long = userFollowRepository.countByFollowingUserId(followingUserId = followingUserId)
        val relationExists: Boolean = userFollowRepository.existsByFollowerUserIdAndFollowingUserId(
            followerUserId = followerUserId,
            followingUserId = followingUserId,
        )

        cacheProvider.put(CacheKeyPrefix.userFollowingCount(followerUserId), followingCount.toString())
        cacheProvider.put(CacheKeyPrefix.userFollowerCount(followingUserId), followerCount.toString())

        val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
        if (relationExists) {
            cacheProvider.put(relationKey, "1")
        } else {
            cacheProvider.delete(relationKey)
        }
    }
}
