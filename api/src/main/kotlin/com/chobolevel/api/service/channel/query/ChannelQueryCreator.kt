package com.chobolevel.api.service.channel.query

import com.chobolevel.domain.channel.ChannelQueryFilter
import com.chobolevel.domain.common.dto.Pagination
import org.springframework.stereotype.Component

@Component
class ChannelQueryCreator {

    fun createQueryFilter(userId: Long?): ChannelQueryFilter {
        return ChannelQueryFilter(
            userId = userId
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
