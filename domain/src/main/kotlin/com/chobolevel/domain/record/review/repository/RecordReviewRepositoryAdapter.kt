package com.chobolevel.domain.record.review.repository

import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.record.review.entity.RecordReview
import org.springframework.stereotype.Component

@Component
class RecordReviewRepositoryAdapter(
    private val recordReviewJpaRepository: RecordReviewJpaRepository
) : RecordReviewRepository {

    override fun findById(id: Long): RecordReview {
        return recordReviewJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.RECORD_REVIEW_NOT_FOUND
        )
    }
}
