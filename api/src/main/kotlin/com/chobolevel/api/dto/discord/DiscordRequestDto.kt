package com.chobolevel.api.dto.discord

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DiscordRequestDto(
    val content: String,
    val username: String,
    val avatarUrl: String,
    val tts: Boolean
)
