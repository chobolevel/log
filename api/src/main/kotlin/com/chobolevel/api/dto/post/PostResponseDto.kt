package com.chobolevel.api.dto.post

import com.chobolevel.api.dto.post.image.PostImageResponseDto
import com.chobolevel.api.dto.tag.TagResponseDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostResponseDto(
    val id: Long = 0,
    val writer: UserResponseDto? = null,
    val tags: List<TagResponseDto>? = null,
    val title: String = "",
    val subTitle: String = "",
    val content: String = "",
    val thumbNailImage: PostImageResponseDto? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
