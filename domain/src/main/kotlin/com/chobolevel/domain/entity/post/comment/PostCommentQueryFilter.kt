package com.chobolevel.domain.entity.post.comment

import com.chobolevel.domain.entity.post.comment.QPostComment.postComment
import com.querydsl.core.types.dsl.BooleanExpression

class PostCommentQueryFilter(
    private val postId: Long?
) {

    fun toPredicates(): Array<BooleanExpression> {
        return listOfNotNull(
            postId?.let { postComment.post.id.eq(it) },
            postComment.deleted.isFalse
        ).toTypedArray()
    }
}
