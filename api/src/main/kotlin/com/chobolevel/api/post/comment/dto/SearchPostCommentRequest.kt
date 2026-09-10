package com.chobolevel.api.post.comment.dto

import com.chobolevel.domain.post.comment.vo.PostCommentOrderType

data class SearchPostCommentRequest(
    val postId: Long?,
    val writerId: Long?,
    val page: Long = 1,
    val size: Long = 50,
    val orderTypes: List<PostCommentOrderType> = emptyList()
)
