package com.chobolevel.api.guest.dto

import jakarta.validation.constraints.NotEmpty

data class DeleteGuestBookRequest(
    @field:NotEmpty(message = "비밀번호는 필수 값입니다.")
    val password: String
)
