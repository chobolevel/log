package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.extension.registerAfterCommit
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.user.follow.dto.UserFollowCounterResponse
import com.chobolevel.api.user.follow.validator.UserFollowBusinessValidator
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFollowService(
    private val userFollowRepository: UserFollowRepository,
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val userFollowBusinessValidator: UserFollowBusinessValidator,
    private val cacheProvider: CacheProvider,
) {

    // 동시 요청에 대한 락은 UserFollowFacade가 이 메서드 호출 전체(커밋까지)를 감싸며 책임진다.
    @Transactional
    fun follow(followerUserId: Long, followingUserId: Long): Boolean {
        userFollowBusinessValidator.validateFollow(followerUserId = followerUserId, followingUserId = followingUserId)

        userFollowSyncEventRepository.save(
            UserFollowSyncEvent.create(
                followerUserId = followerUserId,
                followingUserId = followingUserId,
                action = UserFollowSyncEventAction.FOLLOW,
            )
        )

        val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
        // DB 커밋 성공 후 Redis 반영 — 실패 시 콜드스타트 로직이 eventual하게 복구
        registerAfterCommit {
            cacheProvider.put(relationKey, "1")
            incrementFollowingCount(followerUserId)
            incrementFollowerCount(followingUserId)
        }

        return true
    }

    @Transactional
    fun unfollow(followerUserId: Long, followingUserId: Long): Boolean {
        userFollowBusinessValidator.validateUnfollow(followerUserId = followerUserId, followingUserId = followingUserId)

        userFollowSyncEventRepository.save(
            UserFollowSyncEvent.create(
                followerUserId = followerUserId,
                followingUserId = followingUserId,
                action = UserFollowSyncEventAction.UNFOLLOW,
            )
        )

        val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
        registerAfterCommit {
            cacheProvider.delete(relationKey)
            decrementFollowingCount(followerUserId)
            decrementFollowerCount(followingUserId)
        }

        return true
    }

    private fun incrementFollowingCount(userId: Long) {
        val key: String = CacheKeyPrefix.userFollowingCount(userId)
        initFollowingCountCacheIfAbsent(userId = userId, key = key)
        cacheProvider.increment(key)
    }

    private fun incrementFollowerCount(userId: Long) {
        val key: String = CacheKeyPrefix.userFollowerCount(userId)
        initFollowerCountCacheIfAbsent(userId = userId, key = key)
        cacheProvider.increment(key)
    }

    private fun decrementFollowingCount(userId: Long) {
        val key: String = CacheKeyPrefix.userFollowingCount(userId)
        initFollowingCountCacheIfAbsent(userId = userId, key = key)
        cacheProvider.decrement(key)
    }

    private fun decrementFollowerCount(userId: Long) {
        val key: String = CacheKeyPrefix.userFollowerCount(userId)
        initFollowerCountCacheIfAbsent(userId = userId, key = key)
        cacheProvider.decrement(key)
    }

    // 콜드스타트: Redis에 카운터 키가 없으면 DB COUNT로 시드값 세팅
    private fun initFollowingCountCacheIfAbsent(userId: Long, key: String) {
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.countByFollowerUserId(followerUserId = userId)
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    private fun initFollowerCountCacheIfAbsent(userId: Long, key: String) {
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.countByFollowingUserId(followingUserId = userId)
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    // 관리자 수동 트리거용 복구 경로: afterCommit 콜백의 Redis 호출이 부분 실패해 카운터가 드리프트된 경우,
    // DB COUNT(*)로 다시 계산해 캐시를 강제로 덮어쓴다(콜드스타트의 putIfAbsent와 달리 무조건 덮어씀).
    //
    // user_follows row는 outbox 이벤트가 Kafka 컨슈머에 의해 처리된 뒤에야 반영되므로,
    // 이 유저와 관련된 이벤트가 아직 PROCESSED 상태가 아니라면 COUNT(*)가 그 row를 놓쳐
    // 오히려 정상 값을 더 낮은 값으로 덮어쓸 수 있다. 그래서 미처리 이벤트가 있으면 재계산을 거부한다.
    fun recalculateFollowCounters(userId: Long): UserFollowCounterResponse {
        val hasUnprocessedEvent: Boolean =
            userFollowSyncEventRepository.existsByStatusNotAndFollowerUserId(UserFollowSyncEventStatus.PROCESSED, userId) ||
                userFollowSyncEventRepository.existsByStatusNotAndFollowingUserId(UserFollowSyncEventStatus.PROCESSED, userId)
        if (hasUnprocessedEvent) {
            throw PolicyViolationException(errorCode = ErrorCode.USER_FOLLOW_SYNC_EVENT_NOT_PROCESSED)
        }

        val followingCount: Long = userFollowRepository.countByFollowerUserId(followerUserId = userId)
        val followerCount: Long = userFollowRepository.countByFollowingUserId(followingUserId = userId)
        cacheProvider.put(CacheKeyPrefix.userFollowingCount(userId), followingCount.toString())
        cacheProvider.put(CacheKeyPrefix.userFollowerCount(userId), followerCount.toString())
        return UserFollowCounterResponse(followerCount = followerCount, followingCount = followingCount)
    }
}
