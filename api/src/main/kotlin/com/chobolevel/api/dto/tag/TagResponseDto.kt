package com.chobolevel.api.dto.tag

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TagResponseDto(
    val id: Long,
    val name: String,
    val postsCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)
