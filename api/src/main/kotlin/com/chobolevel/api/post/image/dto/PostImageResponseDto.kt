package com.chobolevel.api.post.image.dto

import com.chobolevel.domain.post.image.entity.PostImageType
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
