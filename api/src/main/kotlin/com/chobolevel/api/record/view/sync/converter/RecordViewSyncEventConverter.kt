package com.chobolevel.api.record.view.sync.converter

import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventResponse
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import org.springframework.stereotype.Component

@Component
class RecordViewSyncEventConverter {

    fun convert(entity: RecordViewSyncEvent): RecordViewSyncEventResponse {
        return RecordViewSyncEventResponse(
            id = entity.id!!,
            recordId = entity.recordId,
            status = entity.status,
            retryCount = entity.retryCount,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            publishedAt = entity.publishedAt?.toInstant()?.toEpochMilli()
        )
    }

    fun convert(entities: List<RecordViewSyncEvent>): List<RecordViewSyncEventResponse> {
        return entities.map { convert(it) }
    }
}
