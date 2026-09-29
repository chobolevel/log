package com.chobolevel.domain.notification.sync.repository

import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.vo.NotificationSyncEventStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationSyncEventJpaRepository : JpaRepository<NotificationSyncEvent, Long> {

    fun findAllByStatus(status: NotificationSyncEventStatus): List<NotificationSyncEvent>

    fun findAllByStatus(status: NotificationSyncEventStatus, pageable: Pageable): List<NotificationSyncEvent>

    fun findAllByStatusOrderByIdAsc(status: NotificationSyncEventStatus, pageable: Pageable): List<NotificationSyncEvent>

    fun countByStatus(status: NotificationSyncEventStatus): Long
}
