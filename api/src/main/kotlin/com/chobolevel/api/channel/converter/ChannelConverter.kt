package com.chobolevel.api.channel.converter

import com.chobolevel.api.channel.dto.ChannelResponseDto
import com.chobolevel.api.channel.dto.CreateChannelRequestDto
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.channel.Channel
import org.springframework.stereotype.Component

@Component
class ChannelConverter(
    private val userConverter: UserConverter
) {

    fun convert(request: CreateChannelRequestDto): Channel {
        return Channel(
            name = request.name
        )
    }

    fun convert(entity: Channel): ChannelResponseDto {
        return ChannelResponseDto(
            id = entity.id!!,
            name = entity.name,
            participants = entity.channelUsers.map { userConverter.convert(it.user!!) },
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Channel>): List<ChannelResponseDto> {
        return entities.map { convert(it) }
    }
}
