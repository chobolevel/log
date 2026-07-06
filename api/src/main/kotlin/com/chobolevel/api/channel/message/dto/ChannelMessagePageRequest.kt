package com.chobolevel.api.channel.message.dto

import com.chobolevel.domain.channel.message.vo.ChannelMessageOrderType

data class ChannelMessagePageRequest(
    val page: Long = 1,
    val size: Long = 50,
    val orderTypes: List<ChannelMessageOrderType> = emptyList()
)
