package com.chobolevel.domain.record.like.repository

import com.chobolevel.domain.record.like.entity.RecordLike
import org.springframework.data.jpa.repository.JpaRepository

interface RecordLikeJpaRepository : JpaRepository<RecordLike, Long> {

    fun countByRecordId(recordId: Long): Long

    fun deleteByRecordIdAndUserId(recordId: Long, userId: Long)

    fun existsByRecordIdAndUserId(recordId: Long, userId: Long): Boolean
}
