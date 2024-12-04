package com.chobolevel.api.controller.v1.channel

import com.chobolevel.api.dto.channel.message.CreateChannelMessageRequestDto
import com.chobolevel.api.service.channel.ChannelMessageService
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
