package com.chobolevel.api.common.dummy

import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.vo.NotificationType
import org.springframework.test.util.ReflectionTestUtils

object DummyNotificationDispatchEvent {
    val ID: Long = 1L
    val USER_ID: Long = DummyUser.ID
    val TYPE: NotificationType = NotificationType.FOLLOW
    val CONTENT: String = "홍길동님이 회원님을 팔로우했습니다."
    val PATH: String = "/users/2"

    fun toEntity(): NotificationDispatchEvent = NotificationDispatchEvent.create(
        userId = USER_ID,
        type = TYPE,
        content = CONTENT,
        path = PATH,
    ).also {
        ReflectionTestUtils.setField(it, "id", ID)
    }
}
