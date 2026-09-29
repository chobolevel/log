package com.chobolevel.api.notification.sse.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.notification.sse.SseEmitterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Notification SSE (알림 실시간 전달)", description = "알림 실시간 전달 API")
@RestController
@RequestMapping("/api/v1")
class NotificationSseController(
    private val sseEmitterRegistry: SseEmitterRegistry,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        // 30분마다 연결이 끊기고 EventSource가 자동 재연결한다 — 커넥션을 무한정 붙잡아두지 않기 위한 상한선
        private const val SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L
    }

    // 이 프로젝트는 쿠키 기반 JWT 인증(OnceJwtAuthorizationFilter)이라, 다른 API와 동일하게
    // @HasAuthorityUser로 보호하면 된다 — EventSource도 쿠키는 보낼 수 있어(withCredentials: true)
    // 별도의 티켓 발급 없이 기존 인증 흐름을 그대로 탄다.
    @Operation(summary = "알림 실시간 구독 API")
    @HasAuthorityUser
    @GetMapping("/notifications/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(authentication: Authentication): SseEmitter {
        val userId: Long = authentication.getUserId()
        val emitter = SseEmitter(SSE_TIMEOUT_MILLIS)

        emitter.onTimeout { emitter.complete() }
        emitter.onError { e -> logger.warn("Notification SSE 연결 오류 - userId: $userId", e) }
        emitter.onCompletion { sseEmitterRegistry.remove(userId) }

        sseEmitterRegistry.register(userId, emitter)
        return emitter
    }
}
