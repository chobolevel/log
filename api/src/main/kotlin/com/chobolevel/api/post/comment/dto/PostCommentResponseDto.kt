package com.chobolevel.api.post.comment.dto

import com.chobolevel.api.user.dto.UserResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostCommentResponseDto(
    val id: Long,
    val writer: UserResponse,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
