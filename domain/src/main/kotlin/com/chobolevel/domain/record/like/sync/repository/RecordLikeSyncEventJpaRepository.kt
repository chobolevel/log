package com.chobolevel.domain.record.like.sync.repository

import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import org.springframework.data.jpa.repository.JpaRepository

interface RecordLikeSyncEventJpaRepository : JpaRepository<RecordLikeSyncEvent, Long> {

    fun findAllByStatus(status: RecordLikeSyncEventStatus): List<RecordLikeSyncEvent>
}
