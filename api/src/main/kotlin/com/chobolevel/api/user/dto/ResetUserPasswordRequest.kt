package com.chobolevel.api.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ResetUserPasswordRequest(
    @field:NotBlank(message = "이메일은 필수 값입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,
    @field:NotBlank(message = "코드는 필수 값입니다.")
    val code: String,
    @field:NotBlank(message = "초기화할 비밀번호는 필수 값입니다.")
    val password: String,
)
