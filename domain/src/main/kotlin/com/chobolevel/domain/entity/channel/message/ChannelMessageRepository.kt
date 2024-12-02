package com.chobolevel.domain.entity.channel.message

import org.springframework.data.jpa.repository.JpaRepository

interface ChannelMessageRepository : JpaRepository<ChannelMessage, Long> {

    fun findByIdAndDeletedFalse(id: Long): ChannelMessage?
}
