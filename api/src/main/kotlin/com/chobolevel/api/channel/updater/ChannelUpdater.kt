package com.chobolevel.api.channel.updater

import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.user.entity.ChannelUser
import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class ChannelUpdater(
    private val userRepository: UserRepository
) {

    fun markAsUpdate(request: UpdateChannelRequest, entity: Channel): Channel {
        request.updateMask.forEach {
            when (it) {
                ChannelUpdateMask.NAME -> entity.name = request.name!!
                ChannelUpdateMask.USERS -> {
                    entity.channelUsers.forEach { it.delete() }
                    request.userIds!!.map { userId ->
                        val user: User = userRepository.findById(userId)
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
