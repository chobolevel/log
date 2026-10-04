package com.chobolevel.domain.notification.exception

import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.ErrorType

enum class NotificationErrorCode(
    override val type: ErrorType,
    override val defaultMessage: String
) : BusinessErrorCode {
    // NOTIFICATION
    NOTIFICATION_NOT_FOUND(ErrorType.NOT_FOUND, "알림을 찾을 수 없습니다."),
    RESTRICTED_TO_NOTIFICATION_OWNER(ErrorType.FORBIDDEN, "알림 수신자만 접근 가능합니다."),

    // NOTIFICATION DISPATCH EVENT
    NOTIFICATION_DISPATCH_EVENT_NOT_FOUND(ErrorType.NOT_FOUND, "알림 디스패치 이벤트를 찾을 수 없습니다.")
}
