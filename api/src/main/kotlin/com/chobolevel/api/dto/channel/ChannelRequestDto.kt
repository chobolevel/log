package com.chobolevel.api.dto.channel

import com.chobolevel.domain.entity.channel.ChannelUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateChannelRequestDto(
    @field:NotEmpty(message = "채널 이름은 필수입니다.")
    val name: String,
    @field:Size(min = 1, message = "최소 한 명이상 초대해야합니다.")
    var userIds: List<Long>
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateChannelRequestDto(
    val name: String?,
    val userIds: List<Long>?,
    @field:Size(min = 1, message = "update_mask는 필수입니다.")
    val updateMask: List<ChannelUpdateMask>
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class InviteChannelRequestDto(
    @field:Size(min = 1, message = "최소 한 명이상 초대해야합니다.")
    val userIds: List<Long>
)
