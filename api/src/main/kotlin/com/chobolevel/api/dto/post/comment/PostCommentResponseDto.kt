package com.chobolevel.api.dto.post.comment

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostCommentResponseDto(
    val id: Long,
    val writerName: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
