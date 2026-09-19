package com.chobolevel.api.user.follow.dto

import com.chobolevel.api.user.dto.UserSummaryResponse

data class UserFollowResponse(
    val user: UserSummaryResponse,
    val createdAt: Long,
)
