package com.chobolevel.api.user.dto

import jakarta.validation.constraints.NotEmpty

data class CheckEmailVerificationCodeRequest(
    @field:NotEmpty(message = "이메일은 필수 값입니다.")
    val email: String,
    @field:NotEmpty(message = "인증 코드는 필수 값입니다.")
    val verificationCode: String,
)
