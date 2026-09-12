package com.chobolevel.api.record.like.sync.dto

import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction

data class RecordLikeSyncEventMessage(
    val eventId: Long,
    val recordId: Long,
    val userId: Long,
    val action: RecordLikeSyncEventAction,
)
