package com.chobolevel.api.controller.v1.chat

import com.chobolevel.api.dto.chat.ChatMessage
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val template: SimpMessagingTemplate
) {

    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    @MessageMapping("/messages")
    fun message(message: ChatMessage) {
        // 메세지 전송 요청 구독 메서드
        try {
            template.convertAndSend("/sub/rooms/${message.roomId}", message)
        } catch (e: AccessDeniedException) {
            logger.info("access denied")
        }
    }
}
