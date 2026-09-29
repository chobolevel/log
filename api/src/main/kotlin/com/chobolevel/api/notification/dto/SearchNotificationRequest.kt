package com.chobolevel.api.notification.dto

import com.chobolevel.domain.notification.vo.NotificationOrderType

data class SearchNotificationRequest(
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<NotificationOrderType> = emptyList(),
)
