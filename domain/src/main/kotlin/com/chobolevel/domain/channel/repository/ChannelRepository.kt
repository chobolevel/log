package com.chobolevel.domain.channel.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.chobolevel.domain.channel.entity.Channel

interface ChannelRepository : JpaRepository<Channel, Long> {

    fun findByIdAndDeletedFalse(id: Long): Channel?
}
