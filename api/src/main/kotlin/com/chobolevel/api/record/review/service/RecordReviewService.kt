package com.chobolevel.api.record.review.service

import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.api.record.review.updater.RecordReviewUpdater
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.repository.RecordReviewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordReviewService(
    private val recordReviewRepository: RecordReviewRepository,
    private val recordBusinessValidator: RecordBusinessValidator,
    private val recordReviewUpdater: RecordReviewUpdater
) {

    @Transactional
    fun updateRecordReview(userId: Long, reviewId: Long, request: UpdateRecordReviewRequest): Long {
        val review: RecordReview = recordReviewRepository.findById(reviewId)
        recordBusinessValidator.validateWriter(userId, review.record)
        recordReviewUpdater.markAsUpdate(request, review)
        return review.id!!
    }
}
