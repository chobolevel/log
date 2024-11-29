package com.chobolevel.api.dto.chat

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChatMessage(
    val type: ChatMessageType,
    val roomId: String,
    val sender: String,
    val message: String
)

enum class ChatMessageType {
    ENTER,
    TALK,
    EXIT,
    MATCH,
    MATCH_REQUEST
}
