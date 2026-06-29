package com.chobolevel.api.channel.message.converter

import com.chobolevel.api.channel.message.dto.ChannelMessageResponseDto
import com.chobolevel.api.channel.message.dto.CreateChannelMessageRequestDto
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.channel.message.entity.ChannelMessage
import org.springframework.stereotype.Component

@Component
class ChannelMessageConverter(
    private val userConverter: UserConverter
) {

    fun convert(request: CreateChannelMessageRequestDto): ChannelMessage {
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
