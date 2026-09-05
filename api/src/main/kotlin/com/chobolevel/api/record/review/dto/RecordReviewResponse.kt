package com.chobolevel.api.record.review.dto

import com.chobolevel.api.subject.dto.SubjectResponse
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RecordReviewResponse(
    val id: Long,
    val subject: SubjectResponse,
    val rating: BigDecimal,
    val createdAt: Long,
    val updatedAt: Long
)
