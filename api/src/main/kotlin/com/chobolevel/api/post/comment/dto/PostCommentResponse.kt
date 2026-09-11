package com.chobolevel.api.post.comment.dto

import com.chobolevel.api.user.dto.UserSummaryResponse

data class PostCommentResponse(
    val id: Long,
    val writer: UserSummaryResponse,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
