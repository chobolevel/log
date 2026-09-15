package com.chobolevel.api.record.emotion.dto

import com.chobolevel.domain.record.emotion.vo.RecordEmotionUpdateMask
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class UpdateRecordEmotionRequest(
    @field:Min(value = 1, message = "감정 강도는 1 이상이어야 합니다.")
    @field:Max(value = 10, message = "감정 강도는 10 이하이어야 합니다.")
    val intensity: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<RecordEmotionUpdateMask>
)
