package com.chobolevel.api.notification.sse

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SseEmitterRegistry {

    fun register(userId: Long, emitter: SseEmitter)

    // emitter를 함께 받아 "현재 등록된 게 이 emitter일 때만" 제거한다 — register()가 더 새 연결로 교체해버린
    // 이후에, 옛 emitter의 onCompletion 콜백이 뒤늦게 불려도 새 연결을 잘못 지우지 않도록 하기 위함
    fun remove(userId: Long, emitter: SseEmitter)

    fun find(userId: Long): SseEmitter?

    // heartbeat 스케줄러가 순회하며 죽은 연결을 찾아내기 위한 스냅샷
    fun all(): Map<Long, SseEmitter>
}
