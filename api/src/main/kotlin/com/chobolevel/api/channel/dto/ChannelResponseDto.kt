package com.chobolevel.api.channel.dto

import com.chobolevel.api.user.dto.UserResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelResponseDto(
    val id: Long,
    val name: String,
    val participants: List<UserResponse>,
    val createdAt: Long,
    val updatedAt: Long
)
