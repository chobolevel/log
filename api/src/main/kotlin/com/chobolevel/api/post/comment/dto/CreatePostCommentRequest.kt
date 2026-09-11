package com.chobolevel.api.post.comment.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreatePostCommentRequest(
    @field:NotNull(message = "게시글 아이디는 필수 값입니다.")
    val postId: Long,
    @field:NotEmpty(message = "댓글 내용은 필수 값입니다.")
    val content: String
)
