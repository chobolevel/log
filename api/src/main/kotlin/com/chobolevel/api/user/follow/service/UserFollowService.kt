package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.user.follow.validator.UserFollowBusinessValidator
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

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
