package com.chobolevel.api.notification.constant

object NotificationLink {
    fun userProfile(userId: Long): String = "/users/$userId"

    fun recordDetail(recordId: Long): String = "/records/$recordId"
}
