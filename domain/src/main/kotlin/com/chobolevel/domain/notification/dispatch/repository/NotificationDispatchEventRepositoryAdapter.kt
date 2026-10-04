package com.chobolevel.domain.notification.dispatch.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class NotificationDispatchEventRepositoryAdapter(
    private val notificationDispatchEventJpaRepository: NotificationDispatchEventJpaRepository,
) : NotificationDispatchEventRepository {

    override fun save(event: NotificationDispatchEvent): NotificationDispatchEvent {
        return notificationDispatchEventJpaRepository.save(event)
    }

    override fun findById(id: Long): NotificationDispatchEvent {
        return notificationDispatchEventJpaRepository.findById(id).orElseThrow {
            DataNotFoundException(errorCode = NotificationErrorCode.NOTIFICATION_DISPATCH_EVENT_NOT_FOUND)
        }
    }

    override fun findByIdOrNull(id: Long): NotificationDispatchEvent? {
        return notificationDispatchEventJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByStatus(status: NotificationDispatchEventStatus): List<NotificationDispatchEvent> {
        return notificationDispatchEventJpaRepository.findAllByStatus(status = status)
    }

    override fun findAllByStatus(status: NotificationDispatchEventStatus, paging: Paging): List<NotificationDispatchEvent> {
        val pageable = PageRequest.of(
            (paging.page - 1).toInt(),
            paging.size.toInt(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return notificationDispatchEventJpaRepository.findAllByStatus(status = status, pageable = pageable)
    }

    override fun countByStatus(status: NotificationDispatchEventStatus): Long {
        return notificationDispatchEventJpaRepository.countByStatus(status = status)
    }

    override fun findAllByStatusOrderByIdAsc(status: NotificationDispatchEventStatus, limit: Long): List<NotificationDispatchEvent> {
        val pageable = PageRequest.of(0, limit.toInt())
        return notificationDispatchEventJpaRepository.findAllByStatusOrderByIdAsc(status = status, pageable = pageable)
    }
}
