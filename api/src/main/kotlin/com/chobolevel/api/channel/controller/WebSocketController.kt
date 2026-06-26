package com.chobolevel.api.channel.controller

import com.chobolevel.api.channel.dto.CreateChannelMessageRequestDto
import com.chobolevel.api.channel.service.ChannelMessageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class WebSocketController(
    private val channelMessageService: ChannelMessageService
) {

    @MessageMapping("/channels/{channelId}/messages")
    fun message(
        @DestinationVariable channelId: Long,
        request: CreateChannelMessageRequestDto,
        accessor: SimpMessageHeaderAccessor
    ) {
        val userId = accessor.sessionAttributes?.get("userId") as Long
        channelMessageService.create(
            userId = userId,
            channelId = channelId,
            request = request
        )
    }
}
