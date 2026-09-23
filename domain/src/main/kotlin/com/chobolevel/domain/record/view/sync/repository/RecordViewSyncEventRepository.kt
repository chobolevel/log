package com.chobolevel.domain.record.view.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus

interface RecordViewSyncEventRepository {

    fun save(event: RecordViewSyncEvent): RecordViewSyncEvent

    fun findById(id: Long): RecordViewSyncEvent

    fun findByIdOrNull(id: Long): RecordViewSyncEvent?

    fun findAllByStatus(status: RecordViewSyncEventStatus): List<RecordViewSyncEvent>

    fun findAllByStatus(status: RecordViewSyncEventStatus, paging: Paging): List<RecordViewSyncEvent>

    fun countByStatus(status: RecordViewSyncEventStatus): Long
}
