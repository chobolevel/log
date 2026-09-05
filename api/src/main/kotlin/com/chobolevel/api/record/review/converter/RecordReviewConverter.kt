package com.chobolevel.api.record.review.converter

import com.chobolevel.api.record.review.dto.RecordReviewResponse
import com.chobolevel.api.subject.converter.SubjectConverter
import com.chobolevel.domain.record.review.entity.RecordReview
import org.springframework.stereotype.Component

@Component
class RecordReviewConverter(
    private val subjectConverter: SubjectConverter
) {

    fun convert(entity: RecordReview): RecordReviewResponse {
        return RecordReviewResponse(
            id = entity.id!!,
            subject = subjectConverter.convert(entity.subject!!),
            rating = entity.rating,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }
}
