package com.chobolevel.domain.post.comment

import com.chobolevel.domain.post.comment.QPostComment.postComment
import com.querydsl.core.types.dsl.BooleanExpression

class PostCommentQueryFilter(
    private val postId: Long?,
    private val writerId: Long?,
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            postId?.let { postComment.post.id.eq(it) },
            writerId?.let { postComment.writer.id.eq(it) },
            postComment.deleted.isFalse
        ).toTypedArray()
    }
}
