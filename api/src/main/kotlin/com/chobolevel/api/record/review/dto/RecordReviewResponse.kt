package com.chobolevel.api.record.review.dto

import com.chobolevel.api.subject.dto.SubjectResponse
import java.math.BigDecimal

data class RecordReviewResponse(
    val id: Long,
    val subject: SubjectResponse,
    val rating: BigDecimal,
    val createdAt: Long,
    val updatedAt: Long
)
