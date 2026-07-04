package com.chobolevel.domain.channel.message.repository

import com.chobolevel.domain.channel.message.entity.ChannelMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelMessageJpaRepository : JpaRepository<ChannelMessage, Long> {

    fun findByIdAndDeletedFalse(id: Long): ChannelMessage?
}
