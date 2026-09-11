package com.chobolevel.api.user.dto

import jakarta.validation.constraints.NotEmpty

data class SendEmailVerificationCodeRequest(
    @field:NotEmpty(message = "이메일은 필수 값입니다.")
    val email: String
)
