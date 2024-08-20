package com.chobolevel.api.dto.auth

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ReissueRequestDto(
    @field:NotEmpty(message = "refresh_token은 필수 값입니다.")
    val refreshToken: String
)
