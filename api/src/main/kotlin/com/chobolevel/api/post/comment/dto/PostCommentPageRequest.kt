package com.chobolevel.api.post.comment.dto

import com.chobolevel.domain.post.comment.entity.PostCommentOrderType

data class PostCommentPageRequest(
    val page: Long = 1,
    val size: Long = 50,
    val orderTypes: List<PostCommentOrderType> = emptyList()
)
