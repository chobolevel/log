package com.chobolevel.api.dto.post

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostTagResponseDto(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
