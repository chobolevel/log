package com.chobolevel.api.dto.channel.message

import com.chobolevel.domain.entity.channel.message.ChannelMessageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateChannelMessageRequest(
    @field:NotNull(message = "메세지 타입은 필수입니다.")
    val type: ChannelMessageType,
    @field:NotEmpty(message = "메세지 내용은 필수입니다.")
    val content: String
)
