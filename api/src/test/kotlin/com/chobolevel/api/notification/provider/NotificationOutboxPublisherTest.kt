package com.chobolevel.api.notification.provider

import com.chobolevel.domain.notification.entity.Notification
import com.chobolevel.domain.notification.repository.NotificationRepository
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import com.chobolevel.domain.notification.vo.NotificationType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifyOrder

class NotificationOutboxPublisherTest : BehaviorSpec({

    val notificationRepository: NotificationRepository = mockk()
    val notificationSyncEventRepository: NotificationSyncEventRepository = mockk()
    val publisher: NotificationOutboxPublisher = NotificationOutboxPublisher(
        notificationRepository = notificationRepository,
        notificationSyncEventRepository = notificationSyncEventRepository,
    )

    beforeEach { clearAllMocks() }

    given("알림을 발행할 때") {
        `when`("push()를 호출하면") {
            then("notifications에 source of truth를 먼저 동기 저장하고, 이어서 notification_sync_events에 outbox 이벤트를 저장한다") {
                // given
                val userId = 1L
                val type: NotificationType = NotificationType.FOLLOW
                val content = "홍길동님이 회원님을 팔로우했습니다."
                val link = "/users/1"
                val notificationSlot: CapturingSlot<Notification> = slot()
                val syncEventSlot: CapturingSlot<NotificationSyncEvent> = slot()
                every { notificationRepository.save(capture(notificationSlot)) } answers { firstArg() }
                every { notificationSyncEventRepository.save(capture(syncEventSlot)) } answers { firstArg() }

                // when
                publisher.publish(userId = userId, type = type, content = content, link = link)

                // then
                verifyOrder {
                    notificationRepository.save(any())
                    notificationSyncEventRepository.save(any())
                }
                notificationSlot.captured.userId shouldBe userId
                notificationSlot.captured.type shouldBe type
                notificationSlot.captured.content shouldBe content
                notificationSlot.captured.link shouldBe link
                syncEventSlot.captured.userId shouldBe userId
                syncEventSlot.captured.type shouldBe type
                syncEventSlot.captured.content shouldBe content
                syncEventSlot.captured.link shouldBe link
            }
        }
    }
})
