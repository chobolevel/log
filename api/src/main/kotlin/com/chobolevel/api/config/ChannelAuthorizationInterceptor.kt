package com.chobolevel.api.config

import com.chobolevel.api.security.TokenProvider
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

@Primary
@Component
class ChannelAuthorizationInterceptor(
    private val tokenProvider: TokenProvider
) : ChannelInterceptor {

    private final val PREFIX = "Bearer "

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECT) {
            val accessToken = accessor.getFirstNativeHeader("Authorization")
            if (accessToken == null || !accessToken.startsWith(PREFIX)) {
                // TODO Authentication exception
                throw ApiException(
                    errorCode = ErrorCode.ALREADY_EXITED_CHANNEL,
                    status = HttpStatus.UNAUTHORIZED,
                    message = "사용 금지"
                )
            }
            val authentication = tokenProvider.getAuthentication(accessToken.substring(PREFIX.length))
            if (authentication == null) {
                // TODO Authentication exception
            }
        }
        return message
    }
}
