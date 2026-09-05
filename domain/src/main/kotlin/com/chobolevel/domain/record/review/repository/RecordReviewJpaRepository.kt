package com.chobolevel.domain.record.review.repository

import com.chobolevel.domain.record.review.entity.RecordReview
import org.springframework.data.jpa.repository.JpaRepository

interface RecordReviewJpaRepository : JpaRepository<RecordReview, Long> {

    fun findByIdAndIsDeletedFalse(id: Long): RecordReview?
}
