package com.chobolevel.api.notification.provider

import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.repository.NotificationRepository
import com.chobolevel.domain.notification.vo.NotificationType
import org.springframework.stereotype.Component

@Component
class NotificationOutboxPublisher(
    private val notificationRepository: NotificationRepository,
    private val notificationDispatchEventRepository: NotificationDispatchEventRepository,
) : NotificationPublisher {

    override fun publish(userId: Long, type: NotificationType, content: String, path: String?) {
        // source of truth: 호출부 트랜잭션 안에서 동기 저장 — Outbox는 이 사실을 외부(SSE)로 전달하는 역할만 한다
        notificationRepository.save(
            Notification.create(
                userId = userId,
                type = type,
                content = content,
                path = path,
            )
        )

        notificationDispatchEventRepository.save(
            NotificationDispatchEvent.create(
                userId = userId,
                type = type,
                content = content,
                path = path,
            )
        )
    }
}
