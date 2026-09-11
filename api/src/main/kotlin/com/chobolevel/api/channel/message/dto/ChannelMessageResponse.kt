package com.chobolevel.api.channel.message.dto

import com.chobolevel.api.user.dto.UserSummaryResponse
import com.chobolevel.domain.channel.message.vo.ChannelMessageType

data class ChannelMessageResponse(
    val id: Long,
    val writer: UserSummaryResponse,
    val type: ChannelMessageType,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
