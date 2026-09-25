package com.chobolevel.api.record.converter

import com.chobolevel.api.common.extension.toMillis
import com.chobolevel.api.record.dto.RecordContributionResponse
import com.chobolevel.api.record.dto.RecordDetailResponse
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.emotion.converter.RecordEmotionConverter
import com.chobolevel.api.record.review.converter.RecordReviewConverter
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordQueryFilter
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class RecordConverter(
    private val userConverter: UserConverter,
    private val recordReviewConverter: RecordReviewConverter,
    private val recordEmotionConverter: RecordEmotionConverter
) {

    fun convert(request: SearchRecordRequest): RecordQueryFilter {
        return RecordQueryFilter(
            userId = request.userId,
            type = request.type,
            title = request.title,
            tagName = request.tagName,
        )
    }

    fun convert(entity: Record, likeCount: Long = 0, viewCount: Long = 0): RecordResponse {
        return RecordResponse(
            id = entity.id!!,
            writer = userConverter.convertToSummary(entity.user),
            type = entity.type,
            title = entity.title,
            isPrivate = entity.isPrivate,
            tags = entity.recordTags.map { it.name },
            review = entity.recordReview?.let { recordReviewConverter.convert(it) },
            emotion = entity.recordEmotion?.let { recordEmotionConverter.convert(it) },
            likeCount = likeCount,
            viewCount = viewCount,
            createdAt = entity.createdAt.toMillis(),
            updatedAt = entity.updatedAt.toMillis()
        )
    }

    fun convert(
        entities: List<Record>,
        likeCounts: Map<Long, Long> = emptyMap(),
        viewCounts: Map<Long, Long> = emptyMap()
    ): List<RecordResponse> {
        return entities.map { convert(it, likeCounts[it.id] ?: 0L, viewCounts[it.id] ?: 0L) }
    }

    fun convertToDetail(record: Record, likeCount: Long = 0, viewCount: Long = 0): RecordDetailResponse {
        return RecordDetailResponse(
            id = record.id!!,
            writer = userConverter.convertToSummary(record.user),
            type = record.type,
            title = record.title,
            content = record.content,
            isPrivate = record.isPrivate,
            tags = record.recordTags.map { it.name },
            review = record.recordReview?.let { recordReviewConverter.convert(it) },
            emotion = record.recordEmotion?.let { recordEmotionConverter.convert(it) },
            likeCount = likeCount,
            viewCount = viewCount,
            createdAt = record.createdAt.toMillis(),
            updatedAt = record.updatedAt.toMillis()
        )
    }

    // year 1/1 ~ 12/31 전체를 채워서 반환한다 — 기록이 없는 날짜는 count 0
    fun convertToContributions(year: Int, countsByDate: Map<LocalDate, Long>): List<RecordContributionResponse> {
        val startOfYear: LocalDate = LocalDate.of(year, 1, 1)
        val endOfYear: LocalDate = LocalDate.of(year, 12, 31)
        return generateSequence(startOfYear) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endOfYear) }
            .map { date -> RecordContributionResponse(date = date.toString(), count = countsByDate[date] ?: 0L) }
            .toList()
    }
}
