package com.chobolevel.api.service.client.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.client.ClientQueryFilter
import org.springframework.stereotype.Component

@Component
class ClientQueryCreator {

    fun createQueryFilter(userId: Long?): ClientQueryFilter {
        return ClientQueryFilter(
            userId = userId
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val skip = skipCount ?: 0
        val limit = limitCount ?: 10
        return Pagination(
            skip = skip,
            limit = limit
        )
    }
}
