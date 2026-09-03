package com.chobolevel.api.user.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginRequest(
    @field:NotEmpty(message = "아이디(이메일)는 필수 값입니다.")
    val email: String,
    @field:NotEmpty(message = "비밀번호는 필수 값입니다.")
    val password: String
)
