package com.chobolevel.api.record.like.sync.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.record.like.sync.converter.RecordLikeSyncEventConverter
import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventResponse
import com.chobolevel.api.record.like.sync.dto.SearchRecordLikeSyncEventRequest
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.repository.RecordLikeSyncEventRepository
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordLikeSyncEventService(
    private val recordLikeSyncEventRepository: RecordLikeSyncEventRepository,
    private val recordLikeSyncEventConverter: RecordLikeSyncEventConverter,
) {

    @Transactional(readOnly = true)
    fun searchFailedEvents(request: SearchRecordLikeSyncEventRequest): PagingResponse<RecordLikeSyncEventResponse> {
        val paging = Paging(page = request.page, size = request.size)
        val events: List<RecordLikeSyncEvent> = recordLikeSyncEventRepository.findAllByStatus(
            status = RecordLikeSyncEventStatus.FAILED,
            paging = paging
        )
        val totalCount: Long = recordLikeSyncEventRepository.countByStatus(RecordLikeSyncEventStatus.FAILED)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = recordLikeSyncEventConverter.convert(entities = events),
            totalCount = totalCount
        )
    }

    @Transactional
    fun retry(eventId: Long): Long {
        val event: RecordLikeSyncEvent = recordLikeSyncEventRepository.findById(eventId)
        if (event.status != RecordLikeSyncEventStatus.FAILED) {
            throw PolicyViolationException(errorCode = ErrorCode.RECORD_LIKE_SYNC_EVENT_NOT_FAILED)
        }
        event.retry()
        return event.id!!
    }
}
