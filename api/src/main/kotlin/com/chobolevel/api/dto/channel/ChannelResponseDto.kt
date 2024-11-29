package com.chobolevel.api.dto.channel

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelResponseDto(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
