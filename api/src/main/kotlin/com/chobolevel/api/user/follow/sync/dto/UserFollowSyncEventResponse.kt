package com.chobolevel.api.user.follow.sync.dto

import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventAction
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus

data class UserFollowSyncEventResponse(
    val id: Long,
    val followerUserId: Long,
    val followingUserId: Long,
    val action: UserFollowSyncEventAction,
    val status: UserFollowSyncEventStatus,
    val retryCount: Int,
    val createdAt: Long,
    val publishedAt: Long?
)
