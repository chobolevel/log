package com.chobolevel.api.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendUserPasswordResetEmailRequest(
    @field:NotBlank(message = "이메일은 필수 값입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,
)
