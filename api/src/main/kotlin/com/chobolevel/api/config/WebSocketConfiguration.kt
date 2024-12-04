package com.chobolevel.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfiguration(
    private val customInterceptor: CustomChannelInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 연결을 위한 엔드포인트 설정
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
        // 브라우저 이슈로 WebSocket 연결이 불가능한 경우 SockJS 통한 연결을 지원
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 메세지를 구독하는 요청 엔드포인트
        registry.enableSimpleBroker("/sub")
        // 메시지를 발생하는 요청 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(customInterceptor)
    }
}
