package com.chobolevel.domain.notification.exception

import com.chobolevel.domain.common.exception.ErrorCode

enum class NotificationErrorCode(override val defaultMessage: String) : ErrorCode {
    // NOTIFICATION
    NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다."),
    RESTRICTED_TO_NOTIFICATION_OWNER("알림 수신자만 접근 가능합니다."),

    // NOTIFICATION DISPATCH EVENT
    NOTIFICATION_DISPATCH_EVENT_NOT_FOUND("알림 디스패치 이벤트를 찾을 수 없습니다.")
}
