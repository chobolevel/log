package com.chobolevel.api.channel.dto

import com.chobolevel.domain.channel.vo.ChannelOrderType

data class ChannelPagingRequest(
    val page: Long = 1,
    val size: Long = 50,
    val orderTypes: List<ChannelOrderType> = emptyList()
)
