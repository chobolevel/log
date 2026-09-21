package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.DistributedLockProvider
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UserFollowFacade(
    private val userFollowService: UserFollowService,
    private val distributedLockProvider: DistributedLockProvider,
) {

    companion object {
        private const val LOCK_WAIT_SECONDS = 5L
        private const val LOCK_LEASE_SECONDS = 10L
    }

    // 락이 UserFollowService의 트랜잭션 전체(커밋까지)를 감싸도록 트랜잭션 프록시 바깥에 위치한다.
    // self-invocation으로 프록시를 우회하지 않도록 userFollowService는 반드시 별도 빈이어야 한다.
    fun follow(followerUserId: Long, followingUserId: Long): Boolean {
        val lockKey: String = CacheKeyPrefix.userFollowLock(followerUserId, followingUserId)
        return distributedLockProvider.executeWithLock(
            key = lockKey,
            waitTime = LOCK_WAIT_SECONDS,
            leaseTime = LOCK_LEASE_SECONDS,
            unit = TimeUnit.SECONDS,
        ) {
            userFollowService.follow(followerUserId = followerUserId, followingUserId = followingUserId)
        }
    }

    fun unfollow(followerUserId: Long, followingUserId: Long): Boolean {
        val lockKey: String = CacheKeyPrefix.userFollowLock(followerUserId, followingUserId)
        return distributedLockProvider.executeWithLock(
            key = lockKey,
            waitTime = LOCK_WAIT_SECONDS,
            leaseTime = LOCK_LEASE_SECONDS,
            unit = TimeUnit.SECONDS,
        ) {
            userFollowService.unfollow(followerUserId = followerUserId, followingUserId = followingUserId)
        }
    }
}
