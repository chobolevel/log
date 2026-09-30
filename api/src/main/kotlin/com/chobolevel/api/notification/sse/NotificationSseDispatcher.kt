package com.chobolevel.api.notification.sse

import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.sync.repository.NotificationSyncEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class NotificationSseDispatcher(
    private val notificationSyncEventRepository: NotificationSyncEventRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Kafka 컨슈머 스레드에서 호출부만 하고 바로 리턴 — 실제 send()는 전용 스레드풀(sseTaskExecutor)에서
    // 실행된다. 성공/실패 여부에 따른 상태 갱신은 여기서 각자 자기 트랜잭션으로 처리한다.
    @Async("sseTaskExecutor")
    @Transactional
    fun dispatch(eventId: Long, userId: Long, emitter: SseEmitter, payload: NotificationSsePayload) {
        runCatching {
            emitter.send(
                SseEmitter.event()
                    .name(SseEventName.NOTIFICATION)
                    .data(payload)
            )
        }.onSuccess {
            notificationSyncEventRepository.findByIdOrNull(eventId)?.markProcessed()
        }.onFailure { e ->
            logger.warn("NotificationSyncEvent SSE push 실패 - eventId: $eventId, userId: $userId", e)
            // send() 실패는 이 커넥션이 죽었다는 뜻이다. completeWithError()를 부르면 NotificationSseController가
            // 걸어둔 onCompletion 콜백이 레지스트리 제거까지 알아서 처리한다 — 여기서 직접 지우면 같은 정리
            // 작업이 두 군데(여기 + onCompletion)에 흩어지므로, 여기서는 "연결을 끊는다"까지만 책임진다
            runCatching { emitter.completeWithError(e) }
            notificationSyncEventRepository.findByIdOrNull(eventId)?.markFailed()
        }
    }

    // heartbeat 전용 — SseHeartbeatScheduler가 자기 스케줄러 스레드에서 직접 send()를 호출하면 느린 클라이언트
    // 하나 때문에 그 tick의 다른 모든 유저 heartbeat까지 밀린다. dispatch()와 동일하게 전용 스레드풀에서
    // 실행하고, 실패 시 completeWithError()로 정리한다(레지스트리 제거는 onCompletion 콜백에 위임).
    @Async("sseTaskExecutor")
    fun dispatchHeartbeat(userId: Long, emitter: SseEmitter) {
        runCatching {
            emitter.send(SseEmitter.event().comment("heartbeat"))
        }.onFailure { e ->
            logger.info("Notification SSE heartbeat 실패, 연결 종료 - userId: $userId")
            runCatching { emitter.completeWithError(e) }
        }
    }
}
