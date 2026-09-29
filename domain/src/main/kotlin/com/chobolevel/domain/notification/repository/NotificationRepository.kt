package com.chobolevel.domain.notification.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.vo.NotificationOrderType
import com.chobolevel.domain.notification.vo.NotificationQueryFilter

interface NotificationRepository {

    fun save(notification: Notification): Notification

    fun findById(id: Long): Notification

    fun findByIdOrNull(id: Long): Notification?

    fun searchNotifications(
        queryFilter: NotificationQueryFilter,
        paging: Paging,
        orderTypes: List<NotificationOrderType>
    ): List<Notification>

    fun searchNotificationsCount(queryFilter: NotificationQueryFilter): Long
}
