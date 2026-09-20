package com.chobolevel.api.user.follow.sync.converter

import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventResponse
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import org.springframework.stereotype.Component

@Component
class UserFollowSyncEventConverter {

    fun convert(entity: UserFollowSyncEvent): UserFollowSyncEventResponse {
        return UserFollowSyncEventResponse(
            id = entity.id!!,
            followerUserId = entity.followerUserId,
            followingUserId = entity.followingUserId,
            action = entity.action,
            status = entity.status,
            retryCount = entity.retryCount,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            publishedAt = entity.publishedAt?.toInstant()?.toEpochMilli()
        )
    }

    fun convert(entities: List<UserFollowSyncEvent>): List<UserFollowSyncEventResponse> {
        return entities.map { convert(it) }
    }
}
