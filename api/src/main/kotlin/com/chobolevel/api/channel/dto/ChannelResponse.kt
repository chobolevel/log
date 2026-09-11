package com.chobolevel.api.channel.dto

import com.chobolevel.api.user.dto.UserSummaryResponse

data class ChannelResponse(
    val id: Long,
    val name: String,
    val participants: List<UserSummaryResponse>,
    val createdAt: Long,
    val updatedAt: Long
)
