package com.chobolevel.api.notification.sync.dto

import com.chobolevel.domain.notification.vo.NotificationType

data class NotificationSyncEventMessage(
    val eventId: Long,
    val userId: Long,
    val type: NotificationType,
    val content: String,
    val link: String?,
)
