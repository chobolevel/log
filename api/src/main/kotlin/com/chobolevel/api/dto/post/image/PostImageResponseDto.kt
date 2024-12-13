package com.chobolevel.api.dto.post.image

import com.chobolevel.domain.entity.post.image.PostImageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostImageResponseDto(
    val id: Long = 0,
    val type: PostImageType = PostImageType.THUMB_NAIL,
    val name: String = "",
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
