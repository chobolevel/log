package com.chobolevel.api.common.dummy

import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.review.dto.CreateRecordReviewRequest
import com.chobolevel.api.record.review.dto.RecordReviewResponse
import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import com.chobolevel.domain.record.vo.RecordType
import com.chobolevel.domain.record.vo.RecordUpdateMask
import org.springframework.test.util.ReflectionTestUtils
import java.math.BigDecimal

object DummyRecord {
    val ID: Long = 1L
    val TYPE: RecordType = RecordType.DIARY
    val TITLE: String = "테스트 기록 제목"
    val CONTENT: String = "테스트 기록 내용"
    val IS_PRIVATE: Boolean = false

    val REVIEW_ID: Long = 2L
    val RATING: BigDecimal = BigDecimal("4.5")

    fun toEntity(): Record = Record.create(
        user = DummyUser.toEntity(),
        type = TYPE,
        title = TITLE,
        content = CONTENT,
        isPrivate = IS_PRIVATE,
        reviewSubject = null,
        reviewRating = null
    ).also {
        ReflectionTestUtils.setField(it, "id", ID)
    }

    fun toEntityWithReview(): Record {
        val record: Record = Record.create(
            user = DummyUser.toEntity(),
            type = RecordType.REVIEW,
            title = TITLE,
            content = CONTENT,
            isPrivate = IS_PRIVATE,
            reviewSubject = DummySubject.toEntity(),
            reviewRating = RATING
        )
        ReflectionTestUtils.setField(record, "id", ID)
        val review: RecordReview = record.recordReview!!
        ReflectionTestUtils.setField(review, "id", REVIEW_ID)
        return record
    }

    fun toCreateRequest(): CreateRecordRequest = CreateRecordRequest(
        type = TYPE,
        title = TITLE,
        content = CONTENT,
        isPrivate = IS_PRIVATE,
        review = null
    )

    fun toCreateReviewRequest(): CreateRecordRequest = CreateRecordRequest(
        type = RecordType.REVIEW,
        title = TITLE,
        content = CONTENT,
        isPrivate = IS_PRIVATE,
        review = CreateRecordReviewRequest(subjectId = DummySubject.ID, rating = RATING)
    )

    fun toUpdateRequest(): UpdateRecordRequest = UpdateRecordRequest(
        type = null,
        title = "새 제목",
        content = null,
        isPrivate = null,
        review = null,
        updateMask = listOf(RecordUpdateMask.TITLE)
    )

    fun toUpdateReviewRequest(): UpdateRecordReviewRequest = UpdateRecordReviewRequest(
        rating = RATING,
        updateMask = listOf(RecordReviewUpdateMask.RATING)
    )

    fun toSearchRequest(): SearchRecordRequest = SearchRecordRequest(
        userId = null,
        type = null,
        title = null
    )

    fun toResponse(): RecordResponse = RecordResponse(
        id = ID,
        writer = DummyUser.toSummaryResponse(),
        type = TYPE,
        title = TITLE,
        content = CONTENT,
        isPrivate = IS_PRIVATE,
        review = null,
        createdAt = 0L,
        updatedAt = 0L
    )

    fun toReviewResponse(): RecordReviewResponse = RecordReviewResponse(
        id = REVIEW_ID,
        subject = DummySubject.toResponse(),
        rating = RATING,
        createdAt = 0L,
        updatedAt = 0L
    )
}
