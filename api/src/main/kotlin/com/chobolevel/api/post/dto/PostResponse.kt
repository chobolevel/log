package com.chobolevel.api.post.dto

import com.chobolevel.api.post.image.dto.PostImageResponse
import com.chobolevel.api.tag.dto.TagResponse
import com.chobolevel.api.user.dto.UserResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostResponse(
    val id: Long = 0,
    val writer: UserResponse? = null,
    val tags: List<TagResponse>? = null,
    val title: String = "",
    val subTitle: String = "",
    val content: String = "",
    val thumbnailImage: PostImageResponse? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
