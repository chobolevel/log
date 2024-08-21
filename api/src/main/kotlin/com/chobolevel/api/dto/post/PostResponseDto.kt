package com.chobolevel.api.dto.post

import com.chobolevel.api.dto.tag.TagResponseDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostResponseDto(
    val id: Long,
    val writer: UserResponseDto,
    val tags: List<TagResponseDto>,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
