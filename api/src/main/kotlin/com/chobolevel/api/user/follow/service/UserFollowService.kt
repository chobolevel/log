package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.common.provider.DistributedLockProvider
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.concurrent.TimeUnit

@Service
class UserFollowService(
    private val userFollowRepository: UserFollowRepository,
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val cacheProvider: CacheProvider,
    private val distributedLockProvider: DistributedLockProvider,
) {

    companion object {
        private const val LOCK_WAIT_SECONDS = 5L
        private const val LOCK_LEASE_SECONDS = 10L
    }

    @Transactional
    fun follow(followerUserId: Long, followingUserId: Long): Boolean {
        val lockKey: String = CacheKeyPrefix.userFollowLock(followerUserId, followingUserId)
        return distributedLockProvider.executeWithLock(
            key = lockKey,
            waitTime = LOCK_WAIT_SECONDS,
            leaseTime = LOCK_LEASE_SECONDS,
            unit = TimeUnit.SECONDS,
        ) {
            val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
            if (isCurrentlyFollowing(followerUserId = followerUserId, followingUserId = followingUserId, relationKey = relationKey)) {
                throw InvalidParameterException(errorCode = ErrorCode.USER_FOLLOW_ALREADY_EXISTS)
            }

            userFollowSyncEventRepository.save(
                UserFollowSyncEvent.create(
                    followerUserId = followerUserId,
                    followingUserId = followingUserId,
                    action = UserFollowSyncEventAction.FOLLOW,
                )
            )

            // DB 커밋 성공 후 Redis 반영 — 실패 시 콜드스타트 로직이 eventual하게 복구
            registerAfterCommit {
                cacheProvider.put(relationKey, "1")
                incrementFollowingCount(followerUserId)
                incrementFollowerCount(followingUserId)
            }

            true
        }
    }

    @Transactional
    fun unfollow(followerUserId: Long, followingUserId: Long): Boolean {
        val lockKey: String = CacheKeyPrefix.userFollowLock(followerUserId, followingUserId)
        return distributedLockProvider.executeWithLock(
            key = lockKey,
            waitTime = LOCK_WAIT_SECONDS,
            leaseTime = LOCK_LEASE_SECONDS,
            unit = TimeUnit.SECONDS,
        ) {
            val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
            if (!isCurrentlyFollowing(followerUserId = followerUserId, followingUserId = followingUserId, relationKey = relationKey)) {
                throw InvalidParameterException(errorCode = ErrorCode.USER_FOLLOW_NOT_FOUND)
            }

            userFollowSyncEventRepository.save(
                UserFollowSyncEvent.create(
                    followerUserId = followerUserId,
                    followingUserId = followingUserId,
                    action = UserFollowSyncEventAction.UNFOLLOW,
                )
            )

            registerAfterCommit {
                cacheProvider.delete(relationKey)
                decrementFollowingCount(followerUserId)
                decrementFollowerCount(followingUserId)
            }

            true
        }
    }

    // 관계 캐시가 없으면 DB로 확인 후 있으면 재적재 (좋아요 Set과 동일한 역할 — TTL 없이 즉시 상태 반영)
    private fun isCurrentlyFollowing(followerUserId: Long, followingUserId: Long, relationKey: String): Boolean {
        if (cacheProvider.hasKey(relationKey)) {
            return true
        }
        val exists: Boolean = userFollowRepository.existsByFollowerUserIdAndFollowingUserId(
            followerUserId = followerUserId,
            followingUserId = followingUserId,
        )
        if (exists) {
            cacheProvider.put(relationKey, "1")
        }
        return exists
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
            val count: Long = userFollowRepository.searchUserFollowsCount(
                UserFollowQueryFilter(followerUserId = userId, followingUserId = null, nickname = null)
            )
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    private fun initFollowerCountCacheIfAbsent(userId: Long, key: String) {
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.searchUserFollowsCount(
                UserFollowQueryFilter(followerUserId = null, followingUserId = userId, nickname = null)
            )
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    private fun registerAfterCommit(action: () -> Unit) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() = action()
            })
        } else {
            action()
        }
    }
}
