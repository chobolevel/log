package com.chobolevel.domain.record.like.repository

import com.chobolevel.domain.record.like.entity.RecordLike

interface RecordLikeRepository {

    fun save(recordLike: RecordLike): RecordLike

    fun findAllByRecordId(recordId: Long): List<RecordLike>

    fun deleteByRecordIdAndUserId(recordId: Long, userId: Long)
}
