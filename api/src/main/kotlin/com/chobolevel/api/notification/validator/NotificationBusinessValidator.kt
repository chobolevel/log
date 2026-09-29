package com.chobolevel.api.notification.validator

import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.notification.entity.Notification
import org.springframework.stereotype.Component

@Component
class NotificationBusinessValidator {

    fun validateOwner(userId: Long, notification: Notification) {
        if (notification.userId != userId) {
            throw ForbiddenException(errorCode = ErrorCode.RESTRICTED_TO_NOTIFICATION_OWNER)
        }
    }
}
