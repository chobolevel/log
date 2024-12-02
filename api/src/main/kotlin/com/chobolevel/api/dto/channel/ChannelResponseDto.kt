package com.chobolevel.api.dto.channel

import com.chobolevel.api.dto.channel.message.ChannelMessageResponseDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.domain.entity.user.User
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelResponseDto(
    val id: Long,
    val name: String,
    val participants: List<UserResponseDto>,
    val createdAt: Long,
    val updatedAt: Long
)
