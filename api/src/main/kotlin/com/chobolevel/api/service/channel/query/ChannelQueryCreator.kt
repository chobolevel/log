package com.chobolevel.api.service.channel.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.channel.ChannelQueryFilter
import org.springframework.stereotype.Component

@Component
class ChannelQueryCreator {

    fun createQueryFilter(userId: Long?): ChannelQueryFilter {
        return ChannelQueryFilter(
            userId = userId
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
