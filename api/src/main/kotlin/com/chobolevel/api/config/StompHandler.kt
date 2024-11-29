package com.chobolevel.api.config

import com.chobolevel.api.controller.v1.chat.ChatController
import com.chobolevel.api.security.TokenProvider
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

@Component
class StompHandler(
    private val tokenProvider: TokenProvider
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECTED) {
            logger.info("===== connected =====")
            if (!accessor.getFirstNativeHeader("Authorization").isNullOrEmpty()) {
                logger.info("access_token: ${accessor.getFirstNativeHeader("Authorization")}")
                tokenProvider.validateToken(accessor.getFirstNativeHeader("Authorization")!!)
            }
        }
        return message
    }
}
