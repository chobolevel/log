package com.chobolevel.api.channel.message.dto

import com.chobolevel.domain.channel.message.vo.ChannelMessageType

data class CreateChannelMessageRequest(
    val type: ChannelMessageType,
    val content: String
)
