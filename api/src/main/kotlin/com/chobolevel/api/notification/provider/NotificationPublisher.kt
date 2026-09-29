package com.chobolevel.api.notification.provider

import com.chobolevel.domain.notification.vo.NotificationType

interface NotificationPublisher {

    fun publish(userId: Long, type: NotificationType, content: String, link: String?)
}
