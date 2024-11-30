package com.chobolevel.api.service.channel.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.message.ChannelMessageQueryFilter
import org.springframework.stereotype.Component

@Component
class ChannelMessageQueryCreator {

    fun createQueryFilter(channelId: Long?): ChannelMessageQueryFilter {
        return ChannelMessageQueryFilter(
            channelId = channelId,
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val skip = skipCount ?: 0
        val limit = limitCount ?: 50
        return Pagination(
            skip = skip,
            limit = limit
        )
    }
}
