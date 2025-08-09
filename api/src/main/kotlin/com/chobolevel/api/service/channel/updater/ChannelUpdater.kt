package com.chobolevel.api.service.channel.updater

import com.chobolevel.api.dto.channel.UpdateChannelRequestDto
import com.chobolevel.domain.entity.channel.Channel
import com.chobolevel.domain.entity.channel.ChannelUpdateMask
import com.chobolevel.domain.entity.channel.user.ChannelUser
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import org.springframework.stereotype.Component

@Component
class ChannelUpdater(
    private val userFinder: UserFinder
) {

    fun markAsUpdate(request: UpdateChannelRequestDto, entity: Channel): Channel {
        request.updateMask.forEach {
            when (it) {
                ChannelUpdateMask.NAME -> entity.name = request.name!!
                ChannelUpdateMask.USERS -> {
                    entity.channelUsers.forEach { it.delete() }
                    request.userIds!!.map { userId ->
                        val user: User = userFinder.findById(userId)
                        ChannelUser().also { channelUser ->
                            channelUser.setBy(user)
                            channelUser.setBy(entity)
                        }
                    }
                }
            }
        }
        return entity
    }
}
