package com.chobolevel.api.common.dummy

import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventResponse
import com.chobolevel.api.record.view.sync.dto.SearchRecordViewSyncEventRequest
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import org.springframework.test.util.ReflectionTestUtils

object DummyRecordViewSyncEvent {
    val ID: Long = 1L
    val RECORD_ID: Long = DummyRecord.ID

    fun toEntity(): RecordViewSyncEvent = RecordViewSyncEvent.create(
        recordId = RECORD_ID
    ).also {
        ReflectionTestUtils.setField(it, "id", ID)
    }

    fun toFailedEntity(): RecordViewSyncEvent = toEntity().apply { markFailed() }

    fun toSearchRequest(): SearchRecordViewSyncEventRequest = SearchRecordViewSyncEventRequest(
        page = 1,
        size = 100
    )

    fun toResponse(): RecordViewSyncEventResponse = RecordViewSyncEventResponse(
        id = ID,
        recordId = RECORD_ID,
        status = RecordViewSyncEventStatus.FAILED,
        retryCount = 1,
        createdAt = 0L,
        publishedAt = null
    )
}
