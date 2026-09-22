package com.chobolevel.domain.record.like.repository

import com.chobolevel.domain.record.like.entity.RecordLike

interface RecordLikeRepository {

    fun save(recordLike: RecordLike): RecordLike

    fun countByRecordId(recordId: Long): Long

    fun existsByRecordIdAndUserId(recordId: Long, userId: Long): Boolean

    fun deleteByRecordIdAndUserId(recordId: Long, userId: Long)
}
