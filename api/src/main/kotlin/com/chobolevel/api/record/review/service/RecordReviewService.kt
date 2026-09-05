package com.chobolevel.api.record.review.service

import com.chobolevel.api.record.review.dto.UpdateRecordReviewRequest
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.record.review.entity.RecordReview
import com.chobolevel.domain.record.review.repository.RecordReviewRepository
import com.chobolevel.domain.record.review.vo.RecordReviewUpdateMask
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordReviewService(
    private val recordReviewRepository: RecordReviewRepository,
    private val recordBusinessValidator: RecordBusinessValidator
) {

    @Transactional
    fun updateRecordReview(userId: Long, reviewId: Long, request: UpdateRecordReviewRequest): Long {
        val review: RecordReview = recordReviewRepository.findById(reviewId)
        recordBusinessValidator.validateWriter(userId, review.record)
        request.updateMask.forEach { mask: RecordReviewUpdateMask ->
            when (mask) {
                RecordReviewUpdateMask.RATING -> review.changeRating(request.rating!!)
            }
        }
        return review.id!!
    }

    @Transactional
    fun deleteRecordReview(userId: Long, reviewId: Long): Boolean {
        val review: RecordReview = recordReviewRepository.findById(reviewId)
        recordBusinessValidator.validateWriter(userId, review.record)
        review.delete()
        return true
    }
}
