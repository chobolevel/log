package com.chobolevel.api.record.dto

import com.chobolevel.api.record.review.dto.RecordReviewResponse
import com.chobolevel.api.user.dto.UserSummaryResponse
import com.chobolevel.domain.record.vo.RecordType

data class RecordResponse(
    val id: Long,
    val writer: UserSummaryResponse,
    val type: RecordType,
    val title: String,
    val content: String,
    val isPrivate: Boolean,
    val review: RecordReviewResponse?,
    val createdAt: Long,
    val updatedAt: Long
)
