package com.chobolevel.domain.notification.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.vo.NotificationSyncEventStatus

interface NotificationSyncEventRepository {

    fun save(event: NotificationSyncEvent): NotificationSyncEvent

    fun findById(id: Long): NotificationSyncEvent

    fun findByIdOrNull(id: Long): NotificationSyncEvent?

    fun findAllByStatus(status: NotificationSyncEventStatus): List<NotificationSyncEvent>

    fun findAllByStatus(status: NotificationSyncEventStatus, paging: Paging): List<NotificationSyncEvent>

    // Relay 스케줄러 전용 — 오래된 순(id ASC)으로 최대 limit개만 가져온다 (풀 스캔/starvation 방지)
    fun findAllByStatusOrderByIdAsc(status: NotificationSyncEventStatus, limit: Long): List<NotificationSyncEvent>

    fun countByStatus(status: NotificationSyncEventStatus): Long
}
