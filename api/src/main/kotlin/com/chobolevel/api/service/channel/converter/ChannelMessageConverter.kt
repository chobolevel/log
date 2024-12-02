package com.chobolevel.api.service.channel.converter

import com.chobolevel.api.dto.channel.message.ChannelMessageResponseDto
import com.chobolevel.api.dto.channel.message.CreateChannelMessageRequest
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.domain.entity.channel.message.ChannelMessage
import org.springframework.stereotype.Component

@Component
class ChannelMessageConverter(
    private val userConverter: UserConverter
) {

    fun convert(request: CreateChannelMessageRequest): ChannelMessage {
        return ChannelMessage(
            type = request.type,
            content = request.content,
        )
    }

    fun convert(entity: ChannelMessage): ChannelMessageResponseDto {
        return ChannelMessageResponseDto(
            id = entity.id!!,
            writer = userConverter.convert(entity.writer!!),
            type = entity.type,
            content = entity.content,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
