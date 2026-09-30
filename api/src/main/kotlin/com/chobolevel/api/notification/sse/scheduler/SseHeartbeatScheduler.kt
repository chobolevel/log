package com.chobolevel.api.notification.sse.scheduler

import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SseHeartbeatScheduler(
    private val sseEmitterRegistry: SseEmitterRegistry,
    private val notificationSseDispatcher: NotificationSseDispatcher,
) {

    companion object {
        // 리버스 프록시/로드밸런서의 idle timeout(보통 60초 이상)보다 짧게, SseEmitter 자체 타임아웃(30분)보다는
        // 훨씬 짧게 잡는다 — 끊긴 연결을 최대한 빨리 찾아서 레지스트리를 실제 상태와 맞추기 위함
        private const val HEARTBEAT_FIXED_DELAY_MILLIS = 15_000L
    }

    // 재연결 시 교체(SseEmitterRegistry.register)만으로는 "아무도 재연결하지 않은 채 그냥 죽은 연결"을
    // 잡아낼 수 없다 — 그런 연결은 여기서 걸러낸다. 실제 send()/정리는 NotificationSseDispatcher에 위임한다
    // (이 스케줄러 스레드에서 직접 보내면 느린 클라이언트 하나가 이번 tick의 다른 모든 유저 몫까지 지연시킨다).
    @Scheduled(fixedDelay = HEARTBEAT_FIXED_DELAY_MILLIS)
    fun heartbeat() {
        sseEmitterRegistry.all().forEach { (userId, emitter) ->
            notificationSseDispatcher.dispatchHeartbeat(userId, emitter)
        }
    }
}
