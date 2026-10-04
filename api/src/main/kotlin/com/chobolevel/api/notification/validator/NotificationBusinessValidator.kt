package com.chobolevel.api.notification.validator

import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import org.springframework.stereotype.Component

@Component
class NotificationBusinessValidator {

    fun validateOwner(userId: Long, notification: Notification) {
        if (notification.userId != userId) {
            throw ForbiddenException(errorCode = NotificationErrorCode.RESTRICTED_TO_NOTIFICATION_OWNER)
        }
    }
}
