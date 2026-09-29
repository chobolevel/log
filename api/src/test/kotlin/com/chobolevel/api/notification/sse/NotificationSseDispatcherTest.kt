package com.chobolevel.api.notification.sse

import com.chobolevel.api.common.dummy.DummyNotificationSyncEvent
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.sync.entity.NotificationSyncEvent
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import com.chobolevel.domain.notification.sync.vo.NotificationSyncEventStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

class NotificationSseDispatcherTest : BehaviorSpec({

    val notificationSyncEventRepository: NotificationSyncEventRepository = mockk()
    val dispatcher = NotificationSseDispatcher(
        notificationSyncEventRepository = notificationSyncEventRepository,
    )

    beforeEach { clearAllMocks() }

    given("SSE로 알림을 전송할 때") {
        `when`("전송이 성공하면") {
            then("이벤트를 PROCESSED로 표시한다") {
                // given
                val event: NotificationSyncEvent = DummyNotificationSyncEvent.toEntity()
                val emitter: SseEmitter = mockk()
                val payload = NotificationSsePayload(
                    type = DummyNotificationSyncEvent.TYPE,
                    content = DummyNotificationSyncEvent.CONTENT,
                    link = DummyNotificationSyncEvent.LINK,
                )
                every { notificationSyncEventRepository.findByIdOrNull(DummyNotificationSyncEvent.ID) } returns event
                justRun { emitter.send(any<SseEmitter.SseEventBuilder>()) }

                // when
                dispatcher.dispatch(
                    eventId = DummyNotificationSyncEvent.ID,
                    userId = DummyNotificationSyncEvent.USER_ID,
                    emitter = emitter,
                    payload = payload,
                )

                // then
                event.status shouldBe NotificationSyncEventStatus.PROCESSED
                verify(exactly = 0) { emitter.completeWithError(any()) }
            }
        }

        `when`("전송이 실패하면") {
            then("emitter를 completeWithError로 종료시키고(레지스트리 정리는 그 onCompletion 콜백에 위임) 이벤트를 FAILED로 표시한다") {
                // given
                val event: NotificationSyncEvent = DummyNotificationSyncEvent.toEntity()
                val emitter: SseEmitter = mockk()
                val payload = NotificationSsePayload(
                    type = DummyNotificationSyncEvent.TYPE,
                    content = DummyNotificationSyncEvent.CONTENT,
                    link = DummyNotificationSyncEvent.LINK,
                )
                every { notificationSyncEventRepository.findByIdOrNull(DummyNotificationSyncEvent.ID) } returns event
                every { emitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")
                justRun { emitter.completeWithError(any()) }

                // when
                dispatcher.dispatch(
                    eventId = DummyNotificationSyncEvent.ID,
                    userId = DummyNotificationSyncEvent.USER_ID,
                    emitter = emitter,
                    payload = payload,
                )

                // then
                event.status shouldBe NotificationSyncEventStatus.FAILED
                verify(exactly = 1) { emitter.completeWithError(any()) }
            }
        }
    }
})
