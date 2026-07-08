package com.chobolevel.api.channel.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class InviteChannelRequest(
    @field:Size(min = 1, message = "최소 한 명이상 초대해야합니다.")
    val userIds: List<Long>
)
