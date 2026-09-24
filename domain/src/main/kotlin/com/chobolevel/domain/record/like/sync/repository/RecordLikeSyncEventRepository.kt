package com.chobolevel.domain.record.like.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus

interface RecordLikeSyncEventRepository {

    fun save(event: RecordLikeSyncEvent): RecordLikeSyncEvent

    fun findById(id: Long): RecordLikeSyncEvent

    fun findByIdOrNull(id: Long): RecordLikeSyncEvent?

    fun findAllByStatus(status: RecordLikeSyncEventStatus): List<RecordLikeSyncEvent>

    fun findAllByStatus(status: RecordLikeSyncEventStatus, paging: Paging): List<RecordLikeSyncEvent>

    // Relay 스케줄러 전용 — 오래된 순(id ASC)으로 최대 limit개만 가져온다 (풀 스캔/starvation 방지)
    fun findAllByStatusOrderByIdAsc(status: RecordLikeSyncEventStatus, limit: Long): List<RecordLikeSyncEvent>

    fun countByStatus(status: RecordLikeSyncEventStatus): Long
}
