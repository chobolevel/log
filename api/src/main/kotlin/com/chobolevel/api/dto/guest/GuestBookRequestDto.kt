package com.chobolevel.api.dto.guest

import com.chobolevel.domain.entity.guest.GuestBookUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class CreateGuestBookRequestDto(
    @field:NotEmpty(message = "방문록 작성자 이름은 필수 값입니다.")
    val guestName: String,
    @field:NotEmpty(message = "방문록 비밀번호는 필수 값입니다.")
    val password: String,
    @field:NotEmpty(message = "방문록 내용을 필수 값입니다.")
    val content: String
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class UpdateGuestBookRequestDto(
    @field:NotEmpty(message = "방문록 수정 시 비밀번호는 필수 값입니다.")
    val password: String,
    val content: String?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<GuestBookUpdateMask>
)
