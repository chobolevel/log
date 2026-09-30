package com.chobolevel.api.notification.sse

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

// 1차 구현 — 인스턴스 로컬 메모리에만 보관한다. 스케일아웃 시 Redis Pub/Sub 기반 구현으로 교체 예정
// ([[project_notification_feature_design]] 참고) — 지금은 단일 인스턴스라 이걸로 충분하다.
@Component
class LocalSseEmitterRegistry : SseEmitterRegistry {

    private val emitters: ConcurrentHashMap<Long, SseEmitter> = ConcurrentHashMap()

    override fun register(userId: Long, emitter: SseEmitter) {
        // 같은 유저의 이전 연결이 남아있으면(새로고침 등으로 이전 EventSource가 안 닫힌 경우) 서버가 먼저 끊어준다
        // — 브라우저의 origin당 동시 연결 제한(HTTP/1.1 기준 통상 6개)에 걸려 이후 모든 요청이 pending되는 걸 방지
        emitters.put(userId, emitter)?.complete()
    }

    override fun remove(userId: Long, emitter: SseEmitter) {
        emitters.remove(userId, emitter)
    }

    override fun find(userId: Long): SseEmitter? {
        return emitters[userId]
    }

    override fun all(): Map<Long, SseEmitter> {
        return emitters.toMap()
    }
}
