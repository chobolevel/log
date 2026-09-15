package com.chobolevel.api.record.dto

import com.chobolevel.api.record.emotion.dto.CreateRecordEmotionRequest
import com.chobolevel.api.record.review.dto.CreateRecordReviewRequest
import com.chobolevel.api.record.vo.RecordTagPolicy
import com.chobolevel.domain.record.vo.RecordType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateRecordRequest(
    @field:NotNull(message = "기록 유형은 필수 값입니다.")
    val type: RecordType,
    @field:NotEmpty(message = "기록 제목은 필수 값입니다.")
    val title: String,
    @field:NotEmpty(message = "기록 내용은 필수 값입니다.")
    val content: String,
    val isPrivate: Boolean = false,
    val tags: List<@Pattern(
            regexp = RecordTagPolicy.NAME_PATTERN,
            message = RecordTagPolicy.NAME_PATTERN_MESSAGE
        ) String> = emptyList(),
    // REVIEW 타입일 때 필수
    @field:Valid
    val review: CreateRecordReviewRequest?,
    // DIARY 타입일 때 필수
    @field:Valid
    val emotion: CreateRecordEmotionRequest?
)
