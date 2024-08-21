package com.chobolevel.api.service.tag.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.tag.TagQueryFilter
import org.springframework.stereotype.Component

@Component
class TagQueryCreator {

    fun createQueryFilter(name: String?): TagQueryFilter {
        return TagQueryFilter(
            name = name
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val skip = skipCount ?: 0
        val limit = limitCount ?: 100
        return Pagination(
            skip = skip,
            limit = limit
        )
    }
}
