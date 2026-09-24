package com.chobolevel.domain.record.view.sync.repository

import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RecordViewSyncEventJpaRepository : JpaRepository<RecordViewSyncEvent, Long> {

    fun findAllByStatus(status: RecordViewSyncEventStatus): List<RecordViewSyncEvent>

    fun findAllByStatus(status: RecordViewSyncEventStatus, pageable: Pageable): List<RecordViewSyncEvent>

    fun findAllByStatusOrderByIdAsc(status: RecordViewSyncEventStatus, pageable: Pageable): List<RecordViewSyncEvent>

    fun countByStatus(status: RecordViewSyncEventStatus): Long
}
