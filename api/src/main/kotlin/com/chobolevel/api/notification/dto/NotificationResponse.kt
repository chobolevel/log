package com.chobolevel.api.notification.dto

import com.chobolevel.domain.notification.vo.NotificationType

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val content: String,
    val link: String?,
    val isRead: Boolean,
    val createdAt: Long,
)
