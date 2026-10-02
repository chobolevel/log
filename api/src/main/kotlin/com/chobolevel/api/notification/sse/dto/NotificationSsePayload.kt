package com.chobolevel.api.notification.sse.dto

import com.chobolevel.domain.notification.vo.NotificationType

data class NotificationSsePayload(
    val type: NotificationType,
    val content: String,
    val path: String?,
)
