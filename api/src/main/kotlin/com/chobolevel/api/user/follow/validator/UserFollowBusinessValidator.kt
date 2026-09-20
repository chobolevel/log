package com.chobolevel.api.user.follow.validator

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFollowBusinessValidator(
    private val userRepository: UserRepository,
    private val userFollowRepository: UserFollowRepository,
    private val cacheProvider: CacheProvider,
) {

    fun validateFollow(followerUserId: Long, followingUserId: Long) {
        if (!userRepository.existsById(followingUserId)) {
            throw DataNotFoundException(errorCode = ErrorCode.USER_NOT_FOUND)
        }
        if (isCurrentlyFollowing(followerUserId = followerUserId, followingUserId = followingUserId)) {
            throw InvalidParameterException(errorCode = ErrorCode.USER_FOLLOW_ALREADY_EXISTS)
        }
    }

    fun validateUnfollow(followerUserId: Long, followingUserId: Long) {
        if (!isCurrentlyFollowing(followerUserId = followerUserId, followingUserId = followingUserId)) {
            throw InvalidParameterException(errorCode = ErrorCode.USER_FOLLOW_NOT_FOUND)
        }
    }

    // 관계 캐시가 없으면 DB로 확인 후 있으면 재적재 (좋아요 Set과 동일한 역할 — TTL 없이 즉시 상태 반영)
    private fun isCurrentlyFollowing(followerUserId: Long, followingUserId: Long): Boolean {
        val relationKey: String = CacheKeyPrefix.userFollowRelation(followerUserId, followingUserId)
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
}
