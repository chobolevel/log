package com.chobolevel.api.channel.dto

import jakarta.validation.constraints.Size

data class InviteChannelRequest(
    @field:Size(min = 1, message = "최소 한 명이상 초대해야합니다.")
    val userIds: List<Long>
)
