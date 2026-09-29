package com.chobolevel.api.notification.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.notification.converter.NotificationConverter
import com.chobolevel.api.notification.dto.NotificationResponse
import com.chobolevel.api.notification.dto.SearchNotificationRequest
import com.chobolevel.api.notification.validator.NotificationBusinessValidator
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.repository.NotificationRepository
import com.chobolevel.domain.notification.vo.NotificationQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationBusinessValidator: NotificationBusinessValidator,
    private val converter: NotificationConverter,
) {

    @Transactional(readOnly = true)
    fun searchNotifications(userId: Long, request: SearchNotificationRequest): PagingResponse<NotificationResponse> {
        val queryFilter = NotificationQueryFilter(userId = userId)
        val paging = Paging(page = request.page, size = request.size)
        val notifications: List<Notification> = notificationRepository.searchNotifications(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = request.orderTypes
        )
        val totalCount: Long = notificationRepository.searchNotificationsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = converter.convert(notifications),
            totalCount = totalCount
        )
    }

    @Transactional
    fun read(userId: Long, notificationId: Long): Boolean {
        val notification: Notification = notificationRepository.findById(notificationId)
        notificationBusinessValidator.validateOwner(userId = userId, notification = notification)
        notification.read()
        return true
    }
}
