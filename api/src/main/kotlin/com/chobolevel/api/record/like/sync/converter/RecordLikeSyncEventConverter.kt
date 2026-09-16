package com.chobolevel.api.record.like.sync.converter

import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventResponse
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import org.springframework.stereotype.Component

@Component
class RecordLikeSyncEventConverter {

    fun convert(entity: RecordLikeSyncEvent): RecordLikeSyncEventResponse {
        return RecordLikeSyncEventResponse(
            id = entity.id!!,
            recordId = entity.recordId,
            userId = entity.userId,
            action = entity.action,
            status = entity.status,
            retryCount = entity.retryCount,
            createdAt = entity.createdAt.toInstant().toEpochMilli(),
            publishedAt = entity.publishedAt?.toInstant()?.toEpochMilli()
        )
    }

    fun convert(entities: List<RecordLikeSyncEvent>): List<RecordLikeSyncEventResponse> {
        return entities.map { convert(it) }
    }
}
