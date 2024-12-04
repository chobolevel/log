package com.chobolevel.api.config

import com.chobolevel.api.getUserId
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
class CustomChannelInterceptor(
    private val tokenProvider: TokenProvider
) : ChannelInterceptor {

    private final val prefix = "Bearer "

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECT || accessor.command == StompCommand.SEND || accessor.command == StompCommand.SUBSCRIBE) {
            val accessToken = accessor.getFirstNativeHeader("Authorization")
            if (accessToken != null && accessToken.startsWith(prefix)) {
                tokenProvider.getAuthentication(accessToken.substring(prefix.length)).also {
                    accessor.sessionAttributes?.put("userId", it?.getUserId())
                }
            } else {
                throw ApiException(
                    errorCode = ErrorCode.INVALID_TOKEN,
                    status = HttpStatus.UNAUTHORIZED,
                    message = "유효하지 않은 토큰입니다."
                )
            }
        }
        return message
    }
}
