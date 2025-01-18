package com.chobolevel.api.dto.tag

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TagResponseDto(
    val id: Long = 0,
    val name: String = "",
    val order: Int = 0,
    val postsCount: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
