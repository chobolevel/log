package com.chobolevel.domain.notification.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import com.chobolevel.domain.notification.vo.NotificationOrderType
import com.chobolevel.domain.notification.vo.NotificationQueryFilter
import org.springframework.stereotype.Component

@Component
class NotificationRepositoryAdapter(
    private val notificationJpaRepository: NotificationJpaRepository,
    private val notificationQuerydslRepository: NotificationQuerydslRepository,
) : NotificationRepository {

    override fun save(notification: Notification): Notification {
        return notificationJpaRepository.save(notification)
    }

    override fun findById(id: Long): Notification {
        return notificationJpaRepository.findById(id).orElseThrow {
            BusinessException(errorCode = NotificationErrorCode.NOTIFICATION_NOT_FOUND)
        }
    }

    override fun findByIdOrNull(id: Long): Notification? {
        return notificationJpaRepository.findById(id).orElse(null)
    }

    override fun searchNotifications(
        queryFilter: NotificationQueryFilter,
        paging: Paging,
        orderTypes: List<NotificationOrderType>
    ): List<Notification> {
        return notificationQuerydslRepository.searchByFilter(
            filter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
    }

    override fun searchNotificationsCount(queryFilter: NotificationQueryFilter): Long {
        return notificationQuerydslRepository.countByFilter(filter = queryFilter)
    }
}
