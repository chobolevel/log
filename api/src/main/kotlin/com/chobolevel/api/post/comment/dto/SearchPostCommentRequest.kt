package com.chobolevel.api.post.comment.dto

data class SearchPostCommentRequest(
    val postId: Long?,
    val writerId: Long?,
)
