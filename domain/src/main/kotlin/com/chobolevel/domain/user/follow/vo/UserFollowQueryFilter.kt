package com.chobolevel.domain.user.follow.vo

data class UserFollowQueryFilter(
    val followerUserId: Long?,
    val followingUserId: Long?,
    val nickname: String?,
)
