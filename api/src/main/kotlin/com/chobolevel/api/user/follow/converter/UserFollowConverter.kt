package com.chobolevel.api.user.follow.converter

import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.follow.dto.SearchUserFollowerRequest
import com.chobolevel.api.user.follow.dto.SearchUserFollowingRequest
import com.chobolevel.api.user.follow.dto.UserFollowResponse
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import org.springframework.stereotype.Component

@Component
class UserFollowConverter(
    private val userConverter: UserConverter,
) {

    fun convert(userId: Long, request: SearchUserFollowerRequest): UserFollowQueryFilter {
        return UserFollowQueryFilter(followerUserId = null, followingUserId = userId, nickname = request.nickname)
    }

    fun convert(userId: Long, request: SearchUserFollowingRequest): UserFollowQueryFilter {
        return UserFollowQueryFilter(followerUserId = userId, followingUserId = null, nickname = request.nickname)
    }

    fun convertToFollowerResponses(entities: List<UserFollow>): List<UserFollowResponse> {
        return entities.map {
            UserFollowResponse(
                user = userConverter.convertToSummary(it.followerUser),
                createdAt = it.createdAt.toInstant().toEpochMilli(),
            )
        }
    }

    fun convertToFollowingResponses(entities: List<UserFollow>): List<UserFollowResponse> {
        return entities.map {
            UserFollowResponse(
                user = userConverter.convertToSummary(it.followingUser),
                createdAt = it.createdAt.toInstant().toEpochMilli(),
            )
        }
    }
}
