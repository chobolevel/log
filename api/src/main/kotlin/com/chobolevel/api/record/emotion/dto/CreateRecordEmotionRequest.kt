package com.chobolevel.api.record.emotion.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateRecordEmotionRequest(
    @field:NotNull(message = "감정 아이디는 필수 값입니다.")
    val emotionId: Long,
    @field:NotNull(message = "감정 강도는 필수 값입니다.")
    @field:Min(value = 1, message = "감정 강도는 1 이상이어야 합니다.")
    @field:Max(value = 10, message = "감정 강도는 10 이하이어야 합니다.")
    val intensity: Int
)
