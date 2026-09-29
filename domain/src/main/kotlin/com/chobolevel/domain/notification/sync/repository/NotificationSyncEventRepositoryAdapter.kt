package com.chobolevel.domain.notification.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.vo.NotificationSyncEventStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class NotificationSyncEventRepositoryAdapter(
    private val notificationSyncEventJpaRepository: NotificationSyncEventJpaRepository,
) : NotificationSyncEventRepository {

    override fun save(event: NotificationSyncEvent): NotificationSyncEvent {
        return notificationSyncEventJpaRepository.save(event)
    }

    override fun findById(id: Long): NotificationSyncEvent {
        return notificationSyncEventJpaRepository.findById(id).orElseThrow {
            DataNotFoundException(errorCode = ErrorCode.NOTIFICATION_SYNC_EVENT_NOT_FOUND)
        }
    }

    override fun findByIdOrNull(id: Long): NotificationSyncEvent? {
        return notificationSyncEventJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByStatus(status: NotificationSyncEventStatus): List<NotificationSyncEvent> {
        return notificationSyncEventJpaRepository.findAllByStatus(status = status)
    }

    override fun findAllByStatus(status: NotificationSyncEventStatus, paging: Paging): List<NotificationSyncEvent> {
        val pageable = PageRequest.of(
            (paging.page - 1).toInt(),
            paging.size.toInt(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return notificationSyncEventJpaRepository.findAllByStatus(status = status, pageable = pageable)
    }

    override fun countByStatus(status: NotificationSyncEventStatus): Long {
        return notificationSyncEventJpaRepository.countByStatus(status = status)
    }

    override fun findAllByStatusOrderByIdAsc(status: NotificationSyncEventStatus, limit: Long): List<NotificationSyncEvent> {
        val pageable = PageRequest.of(0, limit.toInt())
        return notificationSyncEventJpaRepository.findAllByStatusOrderByIdAsc(status = status, pageable = pageable)
    }
}
