package com.chobolevel.api.user.follow.sync.dto

data class SearchUserFollowSyncEventRequest(
    val page: Long = 1,
    val size: Long = 100
)
