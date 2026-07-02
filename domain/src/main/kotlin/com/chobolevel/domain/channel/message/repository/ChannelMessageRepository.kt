package com.chobolevel.domain.channel.message.repository

import com.chobolevel.domain.channel.message.entity.ChannelMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelMessageRepository : JpaRepository<ChannelMessage, Long> {

    fun findByIdAndDeletedFalse(id: Long): ChannelMessage?
}
