package com.chobolevel.domain.channel.repository

import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.vo.ChannelOrderType
import com.chobolevel.domain.channel.vo.ChannelQueryFilter
import com.chobolevel.domain.common.dto.Paging

interface ChannelRepository {

    fun save(channel: Channel): Channel

    fun findById(id: Long): Channel

    fun searchChannels(
        queryFilter: ChannelQueryFilter,
        paging: Paging,
        orderTypes: List<ChannelOrderType>
    ): List<Channel>

    fun searchChannelsCount(queryFilter: ChannelQueryFilter): Long
}
