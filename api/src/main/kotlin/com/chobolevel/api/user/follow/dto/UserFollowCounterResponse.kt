package com.chobolevel.api.user.follow.dto

data class UserFollowCounterResponse(
    val followerCount: Long,
    val followingCount: Long,
)
