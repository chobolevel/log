package com.chobolevel.domain.notification.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.entity.QNotification.notification
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import com.chobolevel.domain.notification.vo.NotificationOrderType
import com.chobolevel.domain.notification.vo.NotificationQueryFilter
import com.querydsl.core.types.OrderSpecifier
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
            DataNotFoundException(errorCode = NotificationErrorCode.NOTIFICATION_NOT_FOUND)
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
        return notificationQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchNotificationsCount(queryFilter: NotificationQueryFilter): Long {
        return notificationQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<NotificationOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                NotificationOrderType.CREATED_AT_ASC -> notification.createdAt.asc()
                NotificationOrderType.CREATED_AT_DESC -> notification.createdAt.desc()
            }
        }.toTypedArray()
    }
}
