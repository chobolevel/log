package com.chobolevel.api.service.post.query

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.comment.PostCommentQueryFilter
import org.springframework.stereotype.Component

@Component
class PostCommentQueryCreator {

    fun createQueryFilter(postId: Long?): PostCommentQueryFilter {
        return PostCommentQueryFilter(
            postId = postId
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
