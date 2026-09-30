package com.chobolevel.api.notification.sse

import com.chobolevel.api.notification.sse.dto.NotificationSsePayload
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executor

@Component
class NotificationSseDispatcher(
    private val sseEmitterRegistry: SseEmitterRegistry,
    private val notificationDispatchEventRepository: NotificationDispatchEventRepository,
    @Qualifier("sseTaskExecutor") private val sseTaskExecutor: Executor,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // 호출부(Consumer)는 userId만 넘기고, 연결 조회는 여기(SSE 배달을 담당하는 쪽)가 책임진다.
    // 연결이 없으면 보낼 게 없으니 그 자리에서 바로 PROCESSED로 표시한다.
    @Async("sseTaskExecutor")
    @Transactional
    fun dispatch(eventId: Long, userId: Long, payload: NotificationSsePayload) {
        // eventId에 대한 검증이 없어서 존재하지 않은 이벤트의 전송이 가능한 상태(이벤트의 수정/삭제가 없어서 문제가 되지 않지만 수정/삭제가 되면 검증 필요)

        val emitter: SseEmitter? = sseEmitterRegistry.find(userId)
        if (emitter == null) {
            notificationDispatchEventRepository.findByIdOrNull(eventId)?.markProcessed()
            return
        }

        runCatching {
            emitter.send(
                SseEmitter.event()
                    .name(SseEventName.NOTIFICATION)
                    .data(payload)
            )
        }.onSuccess {
            notificationDispatchEventRepository.findByIdOrNull(eventId)?.markProcessed()
        }.onFailure { e ->
            logger.warn("NotificationDispatchEvent SSE push 실패 - eventId: $eventId, userId: $userId", e)
            runCatching { emitter.completeWithError(e) }
            notificationDispatchEventRepository.findByIdOrNull(eventId)?.markFailed()
        }
    }

    // heartbeat 팬아웃 — Registry 전체를 순회하되, 각 emitter로의 전송은 sseTaskExecutor에 개별
    // 제출한다. 같은 클래스 안에서 순회하며 자기 자신의 @Async 메서드를 부르면 self-invocation이라
    // 프록시를 안 타고 무시되므로(동기 실행 + 직렬화), Executor에 직접 제출해서 우회한다 — 이렇게 해야
    // 한 emitter가 느려도 나머지 heartbeat 전송이 같이 밀리지 않고 각자 병렬로 처리된다.
    fun dispatchHeartbeat() {
        sseEmitterRegistry.all().forEach { (userId, emitter) ->
            sseTaskExecutor.execute {
                runCatching {
                    emitter.send(SseEmitter.event().comment("heartbeat"))
                }.onFailure { e ->
                    logger.info("Notification SSE heartbeat 실패, 연결 종료 - userId: $userId")
                    runCatching { emitter.completeWithError(e) }
                }
            }
        }
    }
}
