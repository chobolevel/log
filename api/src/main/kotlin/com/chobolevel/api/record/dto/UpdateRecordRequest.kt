package com.chobolevel.api.record.dto

import com.chobolevel.api.record.review.dto.CreateRecordReviewRequest
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.record.vo.RecordUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdateRecordRequest(
    val type: RecordType?,
    val title: String?,
    val content: String?,
    val isPrivate: Boolean?,
    val review: CreateRecordReviewRequest?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<RecordUpdateMask>
)
