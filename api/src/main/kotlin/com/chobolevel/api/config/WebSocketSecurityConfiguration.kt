package com.chobolevel.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager

@Configuration
@EnableWebSocketSecurity
class WebSocketSecurityConfiguration {

    @Bean
    fun authorizeManager(messages: MessageMatcherDelegatingAuthorizationManager.Builder): AuthorizationManager<Message<*>> {
        return messages
            .nullDestMatcher().permitAll()
            .simpTypeMatchers(
                SimpMessageType.CONNECT,
                SimpMessageType.SUBSCRIBE,
                SimpMessageType.DISCONNECT,
                SimpMessageType.MESSAGE
            ).permitAll()
            .anyMessage().denyAll()
            .build()
    }

    @Bean("csrfChannelInterceptor")
    fun csrfChannelInterceptor(): ChannelInterceptor {
        return object : ChannelInterceptor {
        }
    }
}
