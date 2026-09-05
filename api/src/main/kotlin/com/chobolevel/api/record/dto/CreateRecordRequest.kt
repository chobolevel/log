package com.chobolevel.api.record.dto

import com.chobolevel.api.record.review.dto.CreateRecordReviewRequest
import com.chobolevel.domain.record.vo.RecordType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateRecordRequest(
    @field:NotNull(message = "기록 유형은 필수 값입니다.")
    val type: RecordType,
    @field:NotEmpty(message = "기록 제목은 필수 값입니다.")
    val title: String,
    @field:NotEmpty(message = "기록 내용은 필수 값입니다.")
    val content: String,
    val isPrivate: Boolean = false,
    // REVIEW 타입일 때 필수
    val review: CreateRecordReviewRequest?
)
