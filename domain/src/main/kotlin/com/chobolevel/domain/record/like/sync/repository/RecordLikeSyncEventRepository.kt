package com.chobolevel.domain.record.like.sync.repository

import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus

interface RecordLikeSyncEventRepository {

    fun save(event: RecordLikeSyncEvent): RecordLikeSyncEvent

    fun findByIdOrNull(id: Long): RecordLikeSyncEvent?

    fun findAllByStatus(status: RecordLikeSyncEventStatus): List<RecordLikeSyncEvent>
}
