package com.chobolevel.api.user.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SendEmailVerificationCodeRequest(
    @field:NotEmpty(message = "이메일은 필수 값입니다.")
    val email: String
)
