package com.chobolevel.api.record.view.sync.dto

import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus

data class RecordViewSyncEventResponse(
    val id: Long,
    val recordId: Long,
    val status: RecordViewSyncEventStatus,
    val retryCount: Int,
    val createdAt: Long,
    val publishedAt: Long?
)
