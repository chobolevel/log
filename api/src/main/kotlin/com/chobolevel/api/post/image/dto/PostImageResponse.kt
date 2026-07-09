package com.chobolevel.api.post.image.dto

import com.chobolevel.domain.post.image.vo.PostImageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostImageResponse(
    val id: Long,
    val type: PostImageType,
    val name: String,
    val url: String,
    val width: Int,
    val height: Int,
    val createdAt: Long,
    val updatedAt: Long
)
