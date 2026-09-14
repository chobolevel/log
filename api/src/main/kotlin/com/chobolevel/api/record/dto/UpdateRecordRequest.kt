package com.chobolevel.api.record.dto

import com.chobolevel.api.record.review.dto.CreateRecordReviewRequest
import com.chobolevel.api.record.vo.RecordTagPolicy
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.record.vo.RecordUpdateMask
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateRecordRequest(
    val type: RecordType?,
    val title: String?,
    val content: String?,
    val isPrivate: Boolean?,
    val tags: List<@Pattern(
            regexp = RecordTagPolicy.NAME_PATTERN,
            message = RecordTagPolicy.NAME_PATTERN_MESSAGE
        ) String>?,
    val review: CreateRecordReviewRequest?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<RecordUpdateMask>
)
