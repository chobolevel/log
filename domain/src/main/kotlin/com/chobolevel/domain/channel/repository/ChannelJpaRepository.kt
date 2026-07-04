package com.chobolevel.domain.channel.repository

import com.chobolevel.domain.channel.entity.Channel
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelJpaRepository : JpaRepository<Channel, Long> {

    fun findByIdAndDeletedFalse(id: Long): Channel?
}
