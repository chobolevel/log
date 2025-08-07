package com.chobolevel.api.service.channel.query

import com.chobolevel.domain.entity.channel.message.ChannelMessageQueryFilter
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.stereotype.Component

@Component
class ChannelMessageQueryCreator {

    fun createQueryFilter(channelId: Long?): ChannelMessageQueryFilter {
        return ChannelMessageQueryFilter(
            channelId = channelId,
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val offset: Long = skipCount ?: 0
        val limit: Long = limitCount ?: 50
        return Pagination(
            offset = offset,
            limit = limit
        )
    }
}
