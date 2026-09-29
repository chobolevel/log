package com.chobolevel.api.notification.provider

import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.repository.NotificationRepository
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import com.chobolevel.domain.notification.vo.NotificationType
import org.springframework.stereotype.Component

@Component
class NotificationOutboxPublisher(
    private val notificationRepository: NotificationRepository,
    private val notificationSyncEventRepository: NotificationSyncEventRepository,
) : NotificationPublisher {

    override fun publish(userId: Long, type: NotificationType, content: String, link: String?) {
        // source of truth: 호출부 트랜잭션 안에서 동기 저장 — Outbox는 이 사실을 외부(SSE)로 전달하는 역할만 한다
        notificationRepository.save(
            Notification.create(
                userId = userId,
                type = type,
                content = content,
                link = link,
            )
        )

        notificationSyncEventRepository.save(
            NotificationSyncEvent.create(
                userId = userId,
                type = type,
                content = content,
                link = link,
            )
        )
    }
}
