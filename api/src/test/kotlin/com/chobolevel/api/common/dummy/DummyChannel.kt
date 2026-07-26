package com.chobolevel.api.common.dummy

import com.chobolevel.api.channel.dto.ChannelResponse
import com.chobolevel.api.channel.dto.CreateChannelRequest
import com.chobolevel.api.channel.dto.InviteChannelRequest
import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.domain.channel.entity.Channel
import com.chobolevel.domain.channel.vo.ChannelUpdateMask

object DummyChannel {
    val ID: Long = 1L
    val NAME: String = "testChannel"
    val NEW_NAME: String = "newChannelName"
    val INVITE_USER_ID: Long = 2L

    fun toEntity(): Channel = Channel(name = NAME).also {
        it.id = ID
        it.owner = DummyUser.toEntity()
    }

    fun toCreateRequest(): CreateChannelRequest = CreateChannelRequest(
        name = NAME,
        userIds = listOf(INVITE_USER_ID)
    )

    fun toUpdateRequest(): UpdateChannelRequest = UpdateChannelRequest(
        name = NEW_NAME,
        userIds = null,
        updateMask = listOf(ChannelUpdateMask.NAME)
    )

    fun toInviteRequest(): InviteChannelRequest = InviteChannelRequest(
        userIds = listOf(INVITE_USER_ID)
    )

    fun toResponse(): ChannelResponse = ChannelResponse(
        id = ID,
        name = NAME,
        participants = listOf(DummyUser.toResponse()),
        createdAt = 0L,
        updatedAt = 0L
    )
}
