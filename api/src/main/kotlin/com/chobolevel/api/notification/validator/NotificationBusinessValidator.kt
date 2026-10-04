package com.chobolevel.api.notification.validator

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import org.springframework.stereotype.Component

@Component
class NotificationBusinessValidator {

    fun validateOwner(userId: Long, notification: Notification) {
        if (notification.userId != userId) {
            throw BusinessException(errorCode = NotificationErrorCode.RESTRICTED_TO_NOTIFICATION_OWNER)
        }
    }
}
