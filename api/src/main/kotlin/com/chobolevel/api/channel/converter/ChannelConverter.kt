package com.chobolevel.api.channel.converter

import com.chobolevel.api.channel.dto.ChannelResponse
import com.chobolevel.api.channel.dto.CreateChannelRequest
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.channel.entity.Channel
import org.springframework.stereotype.Component

@Component
class ChannelConverter(
    private val userConverter: UserConverter
) {

    fun convert(request: CreateChannelRequest): Channel {
        return Channel(
            name = request.name
        )
    }

    fun convert(entity: Channel): ChannelResponse {
        return ChannelResponse(
            id = entity.id!!,
            name = entity.name,
            participants = entity.channelUsers.map { userConverter.convert(it.user!!) },
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Channel>): List<ChannelResponse> {
        return entities.map { convert(it) }
    }
}
