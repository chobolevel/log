package com.chobolevel.api.record.view.sync.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.record.view.sync.converter.RecordViewSyncEventConverter
import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventResponse
import com.chobolevel.api.record.view.sync.dto.SearchRecordViewSyncEventRequest
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.repository.RecordViewSyncEventRepository
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordViewSyncEventService(
    private val recordViewSyncEventRepository: RecordViewSyncEventRepository,
    private val recordViewSyncEventConverter: RecordViewSyncEventConverter,
) {

    @Transactional(readOnly = true)
    fun searchFailedEvents(request: SearchRecordViewSyncEventRequest): PagingResponse<RecordViewSyncEventResponse> {
        val paging = Paging(page = request.page, size = request.size)
        val events: List<RecordViewSyncEvent> = recordViewSyncEventRepository.findAllByStatus(
            status = RecordViewSyncEventStatus.FAILED,
            paging = paging
        )
        val totalCount: Long = recordViewSyncEventRepository.countByStatus(RecordViewSyncEventStatus.FAILED)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = recordViewSyncEventConverter.convert(entities = events),
            totalCount = totalCount
        )
    }

    @Transactional
    fun retry(eventId: Long): Long {
        val event: RecordViewSyncEvent = recordViewSyncEventRepository.findById(eventId)
        if (event.status != RecordViewSyncEventStatus.FAILED) {
            throw PolicyViolationException(errorCode = ErrorCode.RECORD_VIEW_SYNC_EVENT_NOT_FAILED)
        }
        event.retry()
        return event.id!!
    }
}
