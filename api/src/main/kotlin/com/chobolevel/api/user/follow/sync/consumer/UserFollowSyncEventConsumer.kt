package com.chobolevel.api.user.follow.sync.consumer

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventMessage
import com.chobolevel.domain.common.exception.LogException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserFollowSyncEventConsumer(
    private val userRepository: UserRepository,
    private val userFollowRepository: UserFollowRepository,
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val cacheProvider: CacheProvider,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: Idempotent Consumer]
    //
    // FOLLOW: existsByFollowerUserIdAndFollowingUserId 선확인으로 중복 INSERT 방지 (Kafka retry 시에도 안전)
    // UNFOLLOW: deleteByFollowerUserIdAndFollowingUserId는 존재하지 않는 row 삭제 시 no-op이므로 멱등성 보장
    //
    // 카운터(팔로워/팔로잉 수)는 이미 UserFollowFacade의 락이 UserFollowService 트랜잭션 커밋까지 보호하는
    // 커맨드 시점에 정확히 1번 증감되므로
    // 여기서는 건드리지 않는다. 관계 캐시(userFollowRelation)만 실제 row 생성/삭제에 맞춰 self-healing 차원에서 갱신한다.
    //
    // LogException 계열(회원 삭제 등 데이터 정합성 문제)은 재시도해도 결과가 달라지지 않으므로
    // 재시도 없이 즉시 DLQ로 보낸다.
    @RetryableTopic(
        attempts = "3",
        backoff = Backoff(delay = 1_000, multiplier = 2.0),
        retryTopicSuffix = "-retry",
        dltTopicSuffix = "-dlq",
        exclude = [LogException::class],
        traversingCauses = "true",
    )
    @KafkaListener(topics = [KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS])
    @Transactional
    fun consume(message: UserFollowSyncEventMessage) {
        when (message.action) {
            UserFollowSyncEventAction.FOLLOW -> handleFollow(message)
            UserFollowSyncEventAction.UNFOLLOW -> handleUnfollow(message)
        }
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

    private fun handleFollow(message: UserFollowSyncEventMessage) {
        if (userFollowRepository.existsByFollowerUserIdAndFollowingUserId(message.followerUserId, message.followingUserId)) {
            return
        }
        val followerUser: User = userRepository.findById(message.followerUserId)
        val followingUser: User = userRepository.findById(message.followingUserId)
        val userFollow: UserFollow = followerUser.follow(followingUser)
        userFollowRepository.save(userFollow)

        cacheProvider.put(CacheKeyPrefix.userFollowRelation(message.followerUserId, message.followingUserId), "1")
    }

    private fun handleUnfollow(message: UserFollowSyncEventMessage) {
        userFollowRepository.deleteByFollowerUserIdAndFollowingUserId(
            followerUserId = message.followerUserId,
            followingUserId = message.followingUserId,
        )
        cacheProvider.delete(CacheKeyPrefix.userFollowRelation(message.followerUserId, message.followingUserId))
    }
}
