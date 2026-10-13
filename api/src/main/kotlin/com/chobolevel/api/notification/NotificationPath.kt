package com.chobolevel.api.notification

object NotificationPath {
    fun userProfile(userId: Long): String = "/users/$userId"

    fun recordDetail(recordId: Long): String = "/records/$recordId"
}
