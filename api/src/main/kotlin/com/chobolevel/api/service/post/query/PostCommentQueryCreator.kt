package com.chobolevel.api.service.post.query

import com.chobolevel.domain.entity.post.comment.PostCommentQueryFilter
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.stereotype.Component

@Component
class PostCommentQueryCreator {

    fun createQueryFilter(postId: Long?, writerId: Long?): PostCommentQueryFilter {
        return PostCommentQueryFilter(
            postId = postId,
            writerId = writerId
        )
    }

    fun createPaginationFilter(skipCount: Long?, limitCount: Long?): Pagination {
        val offset = skipCount ?: 0
        val limit = limitCount ?: 50
        return Pagination(
            offset = offset,
            limit = limit
        )
    }
}
