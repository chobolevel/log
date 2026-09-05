package com.chobolevel.domain.record.review.repository

import com.chobolevel.domain.record.review.entity.RecordReview

interface RecordReviewRepository {

    fun findById(id: Long): RecordReview
}
