package com.chobolevel.api.post.comment.query

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
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
