package com.chobolevel.api.channel.dto

import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import jakarta.validation.constraints.Size

data class UpdateChannelRequest(
    val name: String?,
    val userIds: List<Long>?,
    @field:Size(min = 1, message = "update_mask는 필수입니다.")
    val updateMask: List<ChannelUpdateMask>
)
