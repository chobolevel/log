package com.chobolevel.domain.notification.dispatch.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus

interface NotificationDispatchEventRepository {

    fun save(event: NotificationDispatchEvent): NotificationDispatchEvent

    fun findById(id: Long): NotificationDispatchEvent

    fun findByIdOrNull(id: Long): NotificationDispatchEvent?

    fun findAllByStatus(status: NotificationDispatchEventStatus): List<NotificationDispatchEvent>

    fun findAllByStatus(status: NotificationDispatchEventStatus, paging: Paging): List<NotificationDispatchEvent>

    // Relay 스케줄러 전용 — 오래된 순(id ASC)으로 최대 limit개만 가져온다 (풀 스캔/starvation 방지)
    fun findAllByStatusOrderByIdAsc(status: NotificationDispatchEventStatus, limit: Long): List<NotificationDispatchEvent>

    fun countByStatus(status: NotificationDispatchEventStatus): Long
}
