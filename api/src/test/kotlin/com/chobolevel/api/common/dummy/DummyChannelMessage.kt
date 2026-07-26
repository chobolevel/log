package com.chobolevel.api.common.dummy

import com.chobolevel.api.channel.message.dto.ChannelMessageResponse
import com.chobolevel.api.channel.message.dto.CreateChannelMessageRequest
import com.chobolevel.domain.channel.message.entity.ChannelMessage
import com.chobolevel.domain.channel.message.vo.ChannelMessageType

object DummyChannelMessage {
    val ID: Long = 1L
    val CONTENT: String = "testMessage"
    val TYPE: ChannelMessageType = ChannelMessageType.TALK

    fun toEntity(): ChannelMessage = ChannelMessage(
        type = TYPE,
        content = CONTENT
    ).also {
        it.id = ID
        it.writer = DummyUser.toEntity()
    }

    fun toCreateRequest(): CreateChannelMessageRequest = CreateChannelMessageRequest(
        type = TYPE,
        content = CONTENT
    )

    fun toResponse(): ChannelMessageResponse = ChannelMessageResponse(
        id = ID,
        writer = DummyUser.toResponse(),
        type = TYPE,
        content = CONTENT,
        createdAt = 0L,
        updatedAt = 0L
    )
}
