package com.chobolevel.api.service.channel.converter

import com.chobolevel.api.dto.channel.ChannelResponseDto
import com.chobolevel.api.dto.channel.CreateChannelRequestDto
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.domain.entity.channel.Channel
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
}
