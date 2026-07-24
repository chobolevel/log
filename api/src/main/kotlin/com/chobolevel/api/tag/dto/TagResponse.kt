package com.chobolevel.api.tag.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TagResponse(
    val id: Long,
    val name: String,
    val order: Int,
    val postsCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)
