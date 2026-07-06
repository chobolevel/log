package com.chobolevel.api.channel.message.dto

import com.chobolevel.domain.channel.message.vo.ChannelMessageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateChannelMessageRequestDto(
    val type: ChannelMessageType,
    val content: String
)
