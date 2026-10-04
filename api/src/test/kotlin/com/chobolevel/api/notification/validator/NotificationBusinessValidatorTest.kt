package com.chobolevel.api.notification.validator

import com.chobolevel.api.common.dummy.DummyNotification
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.notification.entity.Notification
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class NotificationBusinessValidatorTest : BehaviorSpec({

    val validator: NotificationBusinessValidator = NotificationBusinessValidator()

    given("알림 수신자 검증을 할 때") {
        `when`("요청자가 알림 수신자이면") {
            then("예외가 발생하지 않는다") {
                // given
                val notification: Notification = DummyNotification.toEntity()
                val userId: Long = DummyNotification.USER_ID

                // when & then
                validator.validateOwner(userId = userId, notification = notification)
            }
        }

        `when`("요청자가 알림 수신자가 아니면") {
            then("BusinessException이 발생한다") {
                // given
                val notification: Notification = DummyNotification.toEntity()
                val otherUserId: Long = DummyUser.ID + 1L

                // when & then
                shouldThrow<BusinessException> {
                    validator.validateOwner(userId = otherUserId, notification = notification)
                }
            }
        }
    }
})
