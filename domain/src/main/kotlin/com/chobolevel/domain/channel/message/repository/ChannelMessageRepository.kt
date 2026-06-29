package com.chobolevel.domain.channel.message.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.channel.message.entity.ChannelMessage

interface ChannelMessageRepository : JpaRepository<ChannelMessage, Long> {

    fun findByIdAndDeletedFalse(id: Long): ChannelMessage?
}
