package com.chobolevel.api.user.follow.sync.dto

import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction

data class UserFollowSyncEventMessage(
    val eventId: Long,
    val followerUserId: Long,
    val followingUserId: Long,
    val action: UserFollowSyncEventAction,
)
