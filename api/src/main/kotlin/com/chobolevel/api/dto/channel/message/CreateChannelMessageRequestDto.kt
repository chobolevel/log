package com.chobolevel.api.dto.channel.message

import com.chobolevel.domain.entity.channel.message.ChannelMessageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateChannelMessageRequestDto(
    val type: ChannelMessageType,
    val content: String
)
