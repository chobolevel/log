package com.chobolevel.api.record.dto

import com.chobolevel.api.record.emotion.dto.RecordEmotionResponse
import com.chobolevel.api.record.review.dto.RecordReviewResponse
import com.chobolevel.api.user.dto.UserSummaryResponse
import com.chobolevel.domain.record.vo.RecordType

data class RecordDetailResponse(
    val id: Long,
    val writer: UserSummaryResponse,
    val type: RecordType,
    val title: String,
    val content: String,
    val isPrivate: Boolean,
    val tags: List<String>,
    val review: RecordReviewResponse?,
    val emotion: RecordEmotionResponse?,
    val likeCount: Long = 0,
    val viewCount: Long = 0,
    val createdAt: Long,
    val updatedAt: Long
)
