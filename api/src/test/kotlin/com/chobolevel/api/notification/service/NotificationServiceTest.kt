package com.chobolevel.api.notification.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyNotification
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.notification.converter.NotificationConverter
import com.chobolevel.api.notification.dto.NotificationResponse
import com.chobolevel.api.notification.dto.SearchNotificationRequest
import com.chobolevel.api.notification.validator.NotificationBusinessValidator
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import com.chobolevel.domain.notification.repository.NotificationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotificationServiceTest : BehaviorSpec({

    val notificationRepository: NotificationRepository = mockk()
    val notificationBusinessValidator: NotificationBusinessValidator = mockk()
    val converter: NotificationConverter = mockk()
    val service: NotificationService = NotificationService(
        notificationRepository = notificationRepository,
        notificationBusinessValidator = notificationBusinessValidator,
        converter = converter,
    )

    beforeEach { clearAllMocks() }

    given("알림 목록을 조회할 때") {
        `when`("정상적으로 조회하면") {
            then("PagingResponse로 변환해서 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val notifications: List<Notification> = listOf(DummyNotification.toEntity())
                val responses: List<NotificationResponse> = listOf(DummyNotification.toResponse())
                every {
                    notificationRepository.searchNotifications(queryFilter = any(), paging = any(), orderTypes = any())
                } returns notifications
                every { notificationRepository.searchNotificationsCount(queryFilter = any()) } returns 1L
                every { converter.convert(entities = notifications) } returns responses

                // when
                val result: PagingResponse<NotificationResponse> = service.searchNotifications(
                    userId = userId,
                    request = SearchNotificationRequest()
                )

                // then
                result.totalCount shouldBe 1L
                result.data shouldBe responses
            }
        }
    }

    given("알림을 읽음 처리할 때") {
        `when`("요청자가 알림 수신자이면") {
            then("알림을 읽음 상태로 변경하고 true를 반환한다") {
                // given
                val userId: Long = DummyNotification.USER_ID
                val notification: Notification = DummyNotification.toEntity()
                every { notificationRepository.findById(DummyNotification.ID) } returns notification
                every { notificationBusinessValidator.validateOwner(userId = userId, notification = notification) } returns Unit

                // when
                val result: Boolean = service.read(userId = userId, notificationId = DummyNotification.ID)

                // then
                result shouldBe true
                notification.isRead shouldBe true
                verify(exactly = 1) { notificationBusinessValidator.validateOwner(userId = userId, notification = notification) }
            }
        }

        `when`("요청자가 알림 수신자가 아니면") {
            then("BusinessException이 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1L
                val notification: Notification = DummyNotification.toEntity()
                every { notificationRepository.findById(DummyNotification.ID) } returns notification
                every {
                    notificationBusinessValidator.validateOwner(userId = otherUserId, notification = notification)
                } throws BusinessException(errorCode = NotificationErrorCode.RESTRICTED_TO_NOTIFICATION_OWNER)

                // when & then
                shouldThrow<BusinessException> {
                    service.read(userId = otherUserId, notificationId = DummyNotification.ID)
                }.errorCode shouldBe NotificationErrorCode.RESTRICTED_TO_NOTIFICATION_OWNER
                notification.isRead shouldBe false
            }
        }
    }
})
