package com.chobolevel.api.channel.dto

import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateChannelRequest(
    val name: String?,
    val userIds: List<Long>?,
    @field:Size(min = 1, message = "update_mask는 필수입니다.")
    val updateMask: List<ChannelUpdateMask>
)
