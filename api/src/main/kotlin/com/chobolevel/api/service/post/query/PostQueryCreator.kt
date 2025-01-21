package com.chobolevel.api.service.post.query

import com.chobolevel.domain.entity.post.PostQueryFilter
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.stereotype.Component

@Component
class PostQueryCreator {

    fun createQueryFilter(tagId: Long?, title: String?, subTitle: String?, userId: Long?): PostQueryFilter {
        return PostQueryFilter(
            tagId = tagId,
            title = title,
            subTitle = subTitle,
            userId = userId
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val offset = skipCount ?: 0
        val limit = limitCount ?: 20
        return Pagination(
            offset = offset,
            limit = limit
        )
    }
}
