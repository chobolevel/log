package com.chobolevel.api.channel.message.dto

import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.domain.channel.message.vo.ChannelMessageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelMessageResponseDto(
    val id: Long,
    val writer: UserResponse,
    val type: ChannelMessageType,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
