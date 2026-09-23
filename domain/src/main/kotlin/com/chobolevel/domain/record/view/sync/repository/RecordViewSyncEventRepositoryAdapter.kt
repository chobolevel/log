package com.chobolevel.domain.record.view.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.record.view.sync.entity.RecordViewSyncEvent
import com.chobolevel.domain.record.view.sync.vo.RecordViewSyncEventStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class RecordViewSyncEventRepositoryAdapter(
    private val recordViewSyncEventJpaRepository: RecordViewSyncEventJpaRepository,
) : RecordViewSyncEventRepository {

    override fun save(event: RecordViewSyncEvent): RecordViewSyncEvent {
        return recordViewSyncEventJpaRepository.save(event)
    }

    override fun findById(id: Long): RecordViewSyncEvent {
        return recordViewSyncEventJpaRepository.findById(id).orElseThrow {
            DataNotFoundException(errorCode = ErrorCode.RECORD_VIEW_SYNC_EVENT_NOT_FOUND)
        }
    }

    override fun findByIdOrNull(id: Long): RecordViewSyncEvent? {
        return recordViewSyncEventJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByStatus(status: RecordViewSyncEventStatus): List<RecordViewSyncEvent> {
        return recordViewSyncEventJpaRepository.findAllByStatus(status = status)
    }

    override fun findAllByStatus(status: RecordViewSyncEventStatus, paging: Paging): List<RecordViewSyncEvent> {
        val pageable = PageRequest.of(
            (paging.page - 1).toInt(),
            paging.size.toInt(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return recordViewSyncEventJpaRepository.findAllByStatus(status = status, pageable = pageable)
    }

    override fun countByStatus(status: RecordViewSyncEventStatus): Long {
        return recordViewSyncEventJpaRepository.countByStatus(status = status)
    }
}
