package com.chobolevel.api.service.post.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.tag.PostTagQueryFilter
import org.springframework.stereotype.Component

@Component
class PostTagQueryCreator {

    fun createQueryFilter(name: String?): PostTagQueryFilter {
        return PostTagQueryFilter(
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
