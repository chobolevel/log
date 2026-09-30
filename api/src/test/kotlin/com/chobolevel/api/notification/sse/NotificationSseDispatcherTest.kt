package com.chobolevel.api.notification.sse

import com.chobolevel.api.common.dummy.DummyNotificationDispatchEvent
import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.Executor

class NotificationSseDispatcherTest : BehaviorSpec({

    val sseEmitterRegistry: SseEmitterRegistry = mockk()
    val notificationDispatchEventRepository: NotificationDispatchEventRepository = mockk()
    val sseTaskExecutor: Executor = mockk()
    val dispatcher = NotificationSseDispatcher(
        sseEmitterRegistry = sseEmitterRegistry,
        notificationDispatchEventRepository = notificationDispatchEventRepository,
        sseTaskExecutor = sseTaskExecutor,
    )

    beforeEach { clearAllMocks() }

    given("알림을 SSE로 전송할 때") {
        `when`("수신자가 연결돼 있지 않으면") {
            then("전송 없이 바로 이벤트를 PROCESSED로 표시한다") {
                // given
                val event: NotificationDispatchEvent = DummyNotificationDispatchEvent.toEntity()
                val payload = NotificationSsePayload(
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                every { sseEmitterRegistry.find(DummyNotificationDispatchEvent.USER_ID) } returns null
                every { notificationDispatchEventRepository.findByIdOrNull(DummyNotificationDispatchEvent.ID) } returns event

                // when
                dispatcher.dispatch(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    payload = payload,
                )

                // then
                event.status shouldBe NotificationDispatchEventStatus.PROCESSED
            }
        }

        `when`("전송이 성공하면") {
            then("이벤트를 PROCESSED로 표시한다") {
                // given
                val event: NotificationDispatchEvent = DummyNotificationDispatchEvent.toEntity()
                val emitter: SseEmitter = mockk()
                val payload = NotificationSsePayload(
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                every { sseEmitterRegistry.find(DummyNotificationDispatchEvent.USER_ID) } returns emitter
                every { notificationDispatchEventRepository.findByIdOrNull(DummyNotificationDispatchEvent.ID) } returns event
                justRun { emitter.send(any<SseEmitter.SseEventBuilder>()) }

                // when
                dispatcher.dispatch(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    payload = payload,
                )

                // then
                event.status shouldBe NotificationDispatchEventStatus.PROCESSED
                verify(exactly = 0) { emitter.completeWithError(any()) }
            }
        }

        `when`("전송이 실패하면") {
            then("emitter를 completeWithError로 종료시키고 이벤트를 FAILED로 표시한다") {
                // given
                val event: NotificationDispatchEvent = DummyNotificationDispatchEvent.toEntity()
                val emitter: SseEmitter = mockk()
                val payload = NotificationSsePayload(
                    type = DummyNotificationDispatchEvent.TYPE,
                    content = DummyNotificationDispatchEvent.CONTENT,
                    link = DummyNotificationDispatchEvent.LINK,
                )
                every { sseEmitterRegistry.find(DummyNotificationDispatchEvent.USER_ID) } returns emitter
                every { notificationDispatchEventRepository.findByIdOrNull(DummyNotificationDispatchEvent.ID) } returns event
                every { emitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")
                justRun { emitter.completeWithError(any()) }

                // when
                dispatcher.dispatch(
                    eventId = DummyNotificationDispatchEvent.ID,
                    userId = DummyNotificationDispatchEvent.USER_ID,
                    payload = payload,
                )

                // then
                event.status shouldBe NotificationDispatchEventStatus.FAILED
                verify(exactly = 1) { emitter.completeWithError(any()) }
            }
        }
    }

    given("heartbeat를 전체 연결에 전송할 때") {
        `when`("등록된 emitter가 여럿이면") {
            then("각 전송을 전용 Executor에 개별 제출한다") {
                // given
                val emitter1: SseEmitter = mockk()
                val emitter2: SseEmitter = mockk()
                every { sseEmitterRegistry.all() } returns mapOf(1L to emitter1, 2L to emitter2)
                justRun { emitter1.send(any<SseEmitter.SseEventBuilder>()) }
                justRun { emitter2.send(any<SseEmitter.SseEventBuilder>()) }
                every { sseTaskExecutor.execute(any()) } answers { firstArg<Runnable>().run() }

                // when
                dispatcher.dispatchHeartbeat()

                // then
                verify(exactly = 2) { sseTaskExecutor.execute(any()) }
                verify(exactly = 1) { emitter1.send(any<SseEmitter.SseEventBuilder>()) }
                verify(exactly = 1) { emitter2.send(any<SseEmitter.SseEventBuilder>()) }
            }
        }

        `when`("특정 emitter로의 전송이 실패하면") {
            then("그 emitter만 completeWithError로 종료시키고 나머지는 영향받지 않는다") {
                // given
                val healthyEmitter: SseEmitter = mockk()
                val deadEmitter: SseEmitter = mockk()
                every { sseEmitterRegistry.all() } returns mapOf(1L to healthyEmitter, 2L to deadEmitter)
                justRun { healthyEmitter.send(any<SseEmitter.SseEventBuilder>()) }
                every { deadEmitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("broken pipe")
                justRun { deadEmitter.completeWithError(any()) }
                every { sseTaskExecutor.execute(any()) } answers { firstArg<Runnable>().run() }

                // when
                dispatcher.dispatchHeartbeat()

                // then
                verify(exactly = 0) { healthyEmitter.completeWithError(any()) }
                verify(exactly = 1) { deadEmitter.completeWithError(any()) }
            }
        }
    }
})
