package com.chobolevel.api.notification.converter

import com.chobolevel.api.common.extension.toMillis
import com.chobolevel.api.notification.dto.NotificationResponse
import com.chobolevel.domain.notification.entity.Notification
import org.springframework.stereotype.Component

@Component
class NotificationConverter {

    fun convert(entity: Notification): NotificationResponse {
        return NotificationResponse(
            id = entity.id!!,
            type = entity.type,
            content = entity.content,
            path = entity.path,
            isRead = entity.isRead,
            createdAt = entity.createdAt.toMillis(),
        )
    }

    fun convert(entities: List<Notification>): List<NotificationResponse> {
        return entities.map { convert(it) }
    }
}
