package com.chobolevel.api.guest.dto

import com.chobolevel.domain.guest.vo.GuestBookUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateGuestBookRequest(
    @field:NotEmpty(message = "방문록 수정 시 비밀번호는 필수 값입니다.")
    val password: String,
    val content: String?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<GuestBookUpdateMask>
)
