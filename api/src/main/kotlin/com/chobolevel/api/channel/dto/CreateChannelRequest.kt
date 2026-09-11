package com.chobolevel.api.channel.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateChannelRequest(
    @field:NotEmpty(message = "채널 이름은 필수입니다.")
    val name: String,
    @field:Size(min = 1, message = "최소 한 명이상 초대해야합니다.")
    var userIds: List<Long>
)
