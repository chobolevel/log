package com.chobolevel.api.notification.sse.scheduler

import com.chobolevel.api.notification.sse.NotificationSseDispatcher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SseHeartbeatScheduler(
    private val notificationSseDispatcher: NotificationSseDispatcher,
) {

    companion object {
        // 리버스 프록시/로드밸런서의 idle timeout(보통 60초 이상)보다 짧게, SseEmitter 자체 타임아웃(30분)보다는
        // 훨씬 짧게 잡는다 — 끊긴 연결을 최대한 빨리 찾아서 레지스트리를 실제 상태와 맞추기 위함
        private const val HEARTBEAT_FIXED_DELAY_MILLIS = 15_000L
    }

    // 재연결 시 교체(SseEmitterRegistry.register)만으로는 "아무도 재연결하지 않은 채 그냥 죽은 연결"을
    // 잡아낼 수 없다 — 그런 연결은 여기서 걸러낸다. 누구에게 보낼지(Registry 조회)와 어떻게 보낼지(전송/정리)는
    // 전부 NotificationSseDispatcher 책임이라, 이 스케줄러는 "언제 트리거할지"만 안다.
    @Scheduled(fixedDelay = HEARTBEAT_FIXED_DELAY_MILLIS)
    fun heartbeat() {
        notificationSseDispatcher.dispatchHeartbeat()
    }
}
