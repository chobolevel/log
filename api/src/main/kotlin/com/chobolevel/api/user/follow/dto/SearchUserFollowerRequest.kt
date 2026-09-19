package com.chobolevel.api.user.follow.dto

import com.chobolevel.domain.user.follow.vo.UserFollowOrderType

data class SearchUserFollowerRequest(
    val nickname: String?,
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<UserFollowOrderType> = emptyList()
)
