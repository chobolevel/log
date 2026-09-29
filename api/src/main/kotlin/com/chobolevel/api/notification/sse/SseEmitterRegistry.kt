package com.chobolevel.api.notification.sse

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SseEmitterRegistry {

    fun register(userId: Long, emitter: SseEmitter)

    fun remove(userId: Long)

    fun find(userId: Long): SseEmitter?
}
