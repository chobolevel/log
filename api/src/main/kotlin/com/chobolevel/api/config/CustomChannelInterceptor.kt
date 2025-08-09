package com.chobolevel.api.config

import com.chobolevel.api.getUserId
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
import java.security.Principal

@Primary
@Component
class CustomChannelInterceptor : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor: StompHeaderAccessor = StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECT || accessor.command == StompCommand.SEND || accessor.command == StompCommand.SUBSCRIBE) {
            val user: Principal? = accessor.user
            if (user != null) {
                accessor.sessionAttributes?.put("userId", user.getUserId())
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
