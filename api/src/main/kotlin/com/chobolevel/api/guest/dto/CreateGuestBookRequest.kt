package com.chobolevel.api.guest.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateGuestBookRequest(
    @field:NotEmpty(message = "방문록 작성자 이름은 필수 값입니다.")
    val guestName: String,
    @field:NotEmpty(message = "방문록 비밀번호는 필수 값입니다.")
    val password: String,
    @field:NotEmpty(message = "방문록 내용을 필수 값입니다.")
    val content: String
)
