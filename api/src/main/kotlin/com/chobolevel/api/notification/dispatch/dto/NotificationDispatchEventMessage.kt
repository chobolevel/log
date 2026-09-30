package com.chobolevel.api.notification.dispatch.dto

import com.chobolevel.domain.notification.vo.NotificationType

data class NotificationDispatchEventMessage(
    val eventId: Long,
    val userId: Long,
    val type: NotificationType,
    val content: String,
    val link: String?,
)
