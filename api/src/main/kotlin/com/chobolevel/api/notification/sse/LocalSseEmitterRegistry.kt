package com.chobolevel.api.notification.sse

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

// 1차 구현 — 인스턴스 로컬 메모리에만 보관한다. 스케일아웃 시 Redis Pub/Sub 기반 구현으로 교체 예정
// ([[project_notification_feature_design]] 참고) — 지금은 단일 인스턴스라 이걸로 충분하다.
@Component
class LocalSseEmitterRegistry : SseEmitterRegistry {

    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    override fun register(userId: Long, emitter: SseEmitter) {
        emitters[userId] = emitter
    }

    override fun remove(userId: Long) {
        emitters.remove(userId)
    }

    override fun find(userId: Long): SseEmitter? {
        return emitters[userId]
    }
}
