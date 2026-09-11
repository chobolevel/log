package com.chobolevel.api.record.converter

import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.review.converter.RecordReviewConverter
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordQueryFilter
import org.springframework.stereotype.Component

@Component
class RecordConverter(
    private val userConverter: UserConverter,
    private val recordReviewConverter: RecordReviewConverter
) {

    fun convert(request: SearchRecordRequest): RecordQueryFilter {
        return RecordQueryFilter(
            userId = request.userId,
            type = request.type,
            title = request.title,
        )
    }

    fun convert(entity: Record, likeCount: Long = 0): RecordResponse {
        return RecordResponse(
            id = entity.id!!,
            writer = userConverter.convertToSummary(entity.user!!),
            type = entity.type,
            title = entity.title,
            content = entity.content,
            isPrivate = entity.isPrivate,
            review = entity.recordReview?.let { recordReviewConverter.convert(it) },
            likeCount = likeCount,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Record>, likeCounts: Map<Long, Long> = emptyMap()): List<RecordResponse> {
        return entities.map { convert(it, likeCounts[it.id] ?: 0L) }
    }
}
