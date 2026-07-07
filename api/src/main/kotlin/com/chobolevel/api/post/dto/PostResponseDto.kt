package com.chobolevel.api.post.dto

import com.chobolevel.api.post.image.dto.PostImageResponseDto
import com.chobolevel.api.tag.dto.TagResponseDto
import com.chobolevel.api.user.dto.UserResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostResponseDto(
    val id: Long = 0,
    val writer: UserResponse? = null,
    val tags: List<TagResponseDto>? = null,
    val title: String = "",
    val subTitle: String = "",
    val content: String = "",
    val thumbNailImage: PostImageResponseDto? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
