package com.chobolevel.api.service.tag.query

import com.chobolevel.domain.entity.tag.TagQueryFilter
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.stereotype.Component

@Component
class TagQueryCreator {

    fun createQueryFilter(name: String?): TagQueryFilter {
        return TagQueryFilter(
            name = name
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val offset = skipCount ?: 0
        val limit = limitCount ?: 100
        return Pagination(
            offset = offset,
            limit = limit
        )
    }
}
