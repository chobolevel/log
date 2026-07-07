package com.chobolevel.api.user.dto

import com.chobolevel.domain.user.vo.UserUpdateMask
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    val nickname: String?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<UserUpdateMask>
)
