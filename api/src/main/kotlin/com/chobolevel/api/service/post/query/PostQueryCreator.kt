package com.chobolevel.api.service.post.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.PostQueryFilter
import org.springframework.stereotype.Component

@Component
class PostQueryCreator {

    fun createQueryFilter(tagId: Long?, title: String?, content: String?): PostQueryFilter {
        return PostQueryFilter(
            tagId = tagId,
            title = title,
            content = content
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val skip = skipCount ?: 0
        val limit = limitCount ?: 20
        return Pagination(
            skip = skip,
            limit = limit
        )
    }
}
