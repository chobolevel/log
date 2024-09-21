package com.chobolevel.api.dto.post.image

import com.chobolevel.domain.entity.post.image.PostImageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostImageResponseDto(
    val id: Long,
    val type: PostImageType,
    val name: String,
    val url: String,
    val width: Int,
    val height: Int,
    val createdAt: Long,
    val updatedAt: Long
)
