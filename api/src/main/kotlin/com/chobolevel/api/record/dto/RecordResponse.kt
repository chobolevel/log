package com.chobolevel.api.record.dto

import com.chobolevel.api.record.review.dto.RecordReviewResponse
import com.chobolevel.api.user.dto.UserResponse
import com.chobolevel.domain.record.vo.RecordType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RecordResponse(
    val id: Long,
    val writer: UserResponse,
    val type: RecordType,
    val title: String,
    val content: String,
    val isPrivate: Boolean,
    val review: RecordReviewResponse?,
    val createdAt: Long,
    val updatedAt: Long
)
