package com.chobolevel.domain.entity.channel

import org.springframework.data.jpa.repository.JpaRepository

interface ChannelRepository : JpaRepository<Channel, Long> {

    fun findByIdAndDeletedFalse(id: Long): Channel?
}
