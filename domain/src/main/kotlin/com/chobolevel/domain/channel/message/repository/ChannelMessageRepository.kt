package com.chobolevel.domain.channel.message.repository

import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.vo.ChannelMessageOrderType
import com.chobolevel.domain.channel.message.vo.ChannelMessageQueryFilter
import com.chobolevel.domain.common.dto.Paging

interface ChannelMessageRepository {

    fun save(channelMessage: ChannelMessage): ChannelMessage

    fun findById(id: Long): ChannelMessage

    fun searchChannelMessages(
        queryFilter: ChannelMessageQueryFilter,
        paging: Paging,
        orderTypes: List<ChannelMessageOrderType>
    ): List<ChannelMessage>

    fun searchChannelMessagesCount(queryFilter: ChannelMessageQueryFilter): Long
}
