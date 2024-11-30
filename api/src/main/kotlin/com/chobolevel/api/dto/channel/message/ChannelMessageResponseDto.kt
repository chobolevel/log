package com.chobolevel.api.dto.channel.message

import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.domain.entity.channel.message.ChannelMessageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelMessageResponseDto(
    val id: Long,
    val writer: UserResponseDto,
    val type: ChannelMessageType,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
