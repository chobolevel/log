package com.chobolevel.api.record.like.sync.dto

import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus

data class RecordLikeSyncEventResponse(
    val id: Long,
    val recordId: Long,
    val userId: Long,
    val action: RecordLikeSyncEventAction,
    val status: RecordLikeSyncEventStatus,
    val retryCount: Int,
    val createdAt: Long,
    val publishedAt: Long?
)
