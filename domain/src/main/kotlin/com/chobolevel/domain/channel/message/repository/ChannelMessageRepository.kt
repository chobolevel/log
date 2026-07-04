package com.chobolevel.domain.channel.message.repository

import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.entity.ChannelMessageOrderType
import com.chobolevel.domain.channel.message.vo.ChannelMessageQueryFilter
import com.chobolevel.domain.common.dto.Pagination

interface ChannelMessageRepository {

    fun save(channelMessage: ChannelMessage): ChannelMessage

    fun findById(id: Long): ChannelMessage

    fun searchChannelMessages(
        queryFilter: ChannelMessageQueryFilter,
        pagination: Pagination,
        orderTypes: List<ChannelMessageOrderType>
    ): List<ChannelMessage>

    fun searchChannelMessagesCount(queryFilter: ChannelMessageQueryFilter): Long
}
