package com.chobolevel.api.notification.sse.scheduler

import com.chobolevel.api.notification.sse.SseEmitterRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class SseHeartbeatScheduler(
    private val sseEmitterRegistry: SseEmitterRegistry,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        // 리버스 프록시/로드밸런서의 idle timeout(보통 60초 이상)보다 짧게, SseEmitter 자체 타임아웃(30분)보다는
        // 훨씬 짧게 잡는다 — 끊긴 연결을 최대한 빨리 찾아서 레지스트리를 실제 상태와 맞추기 위함
        private const val HEARTBEAT_FIXED_DELAY_MILLIS = 15_000L
    }

    // 재연결 시 교체(SseEmitterRegistry.register)만으로는 "아무도 재연결하지 않은 채 그냥 죽은 연결"을
    // 잡아낼 수 없다 — 그런 연결은 여기서 걸러낸다. 코멘트 라인만 보내 클라이언트의 이벤트 리스너에는
    // 아무 영향이 없고, 전송 자체가 실패하면 그 자리에서 바로 제거한다.
    @Scheduled(fixedDelay = HEARTBEAT_FIXED_DELAY_MILLIS)
    fun heartbeat() {
        sseEmitterRegistry.all().forEach { (userId, emitter) ->
            runCatching {
                emitter.send(SseEmitter.event().comment("heartbeat"))
            }.onFailure {
                logger.info("Notification SSE heartbeat 실패, 연결 제거 - userId: $userId")
                sseEmitterRegistry.remove(userId, emitter)
            }
        }
    }
}
