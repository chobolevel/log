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

    /**
     * 해당 엔드포인트로 요청 시 소켓 연결
     * @param registry
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 연결을 위한 엔드포인트 설정
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
        // 브라우저 이슈로 WebSocket 연결이 불가능한 경우 SockJS 통한 연결을 지원
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS()
    }

    /**
     * 메시지 브로커 설정을 위한 메소드
     * /sub/channels/{channelId} -> 메세지 구독(수신)
     * /pub/channel-messages -> 메세지 발행(송신)
     * @param registry
     */
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
