package com.chobolevel.domain.notification.dispatch.repository

import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationDispatchEventJpaRepository : JpaRepository<NotificationDispatchEvent, Long> {

    fun findAllByStatus(status: NotificationDispatchEventStatus): List<NotificationDispatchEvent>

    fun findAllByStatus(status: NotificationDispatchEventStatus, pageable: Pageable): List<NotificationDispatchEvent>

    fun findAllByStatusOrderByIdAsc(status: NotificationDispatchEventStatus, pageable: Pageable): List<NotificationDispatchEvent>

    fun countByStatus(status: NotificationDispatchEventStatus): Long
}
