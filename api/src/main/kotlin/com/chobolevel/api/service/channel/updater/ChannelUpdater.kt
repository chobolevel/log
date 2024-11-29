package com.chobolevel.api.service.channel.updater

import com.chobolevel.api.dto.channel.UpdateChannelRequestDto
import com.chobolevel.domain.entity.channel.Channel
import com.chobolevel.domain.entity.channel.ChannelUpdateMask
import org.springframework.stereotype.Component

@Component
class ChannelUpdater {

    fun markAsUpdate(request: UpdateChannelRequestDto, entity: Channel): Channel {
        request.updateMask.forEach {
            when (it) {
                ChannelUpdateMask.NAME -> entity.name = request.name!!
            }
        }
        return entity
    }
}
