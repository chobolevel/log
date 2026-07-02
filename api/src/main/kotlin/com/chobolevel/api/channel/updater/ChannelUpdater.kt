package com.chobolevel.api.channel.updater

import com.chobolevel.api.channel.dto.UpdateChannelRequestDto
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.entity.ChannelUpdateMask
import com.chobolevel.domain.channel.user.entity.ChannelUser
import com.chobolevel.domain.user.UserFinder
import com.chobolevel.domain.user.entity.User
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
