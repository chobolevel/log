package com.chobolevel.api.common.dummy

import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventResponse
import com.chobolevel.api.record.like.sync.dto.SearchRecordLikeSyncEventRequest
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventAction
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import org.springframework.test.util.ReflectionTestUtils

object DummyRecordLikeSyncEvent {
    val ID: Long = 1L
    val RECORD_ID: Long = DummyRecord.ID
    val USER_ID: Long = DummyUser.ID
    val ACTION: RecordLikeSyncEventAction = RecordLikeSyncEventAction.LIKE

    fun toEntity(): RecordLikeSyncEvent = RecordLikeSyncEvent.create(
        recordId = RECORD_ID,
        userId = USER_ID,
        action = ACTION
    ).also {
        ReflectionTestUtils.setField(it, "id", ID)
    }

    fun toFailedEntity(): RecordLikeSyncEvent = toEntity().apply { markFailed() }

    fun toSearchRequest(): SearchRecordLikeSyncEventRequest = SearchRecordLikeSyncEventRequest(
        page = 1,
        size = 100
    )

    fun toResponse(): RecordLikeSyncEventResponse = RecordLikeSyncEventResponse(
        id = ID,
        recordId = RECORD_ID,
        userId = USER_ID,
        action = ACTION,
        status = RecordLikeSyncEventStatus.FAILED,
        retryCount = 1,
        createdAt = 0L,
        publishedAt = null
    )
}
