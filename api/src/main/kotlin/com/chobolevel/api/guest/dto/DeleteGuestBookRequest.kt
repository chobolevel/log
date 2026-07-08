package com.chobolevel.api.guest.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DeleteGuestBookRequest(
    @field:NotEmpty(message = "비밀번호는 필수 값입니다.")
    val password: String
)
