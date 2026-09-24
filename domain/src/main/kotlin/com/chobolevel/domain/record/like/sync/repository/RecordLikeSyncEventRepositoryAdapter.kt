package com.chobolevel.domain.record.like.sync.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class RecordLikeSyncEventRepositoryAdapter(
    private val recordLikeSyncEventJpaRepository: RecordLikeSyncEventJpaRepository,
) : RecordLikeSyncEventRepository {

    override fun save(event: RecordLikeSyncEvent): RecordLikeSyncEvent {
        return recordLikeSyncEventJpaRepository.save(event)
    }

    override fun findById(id: Long): RecordLikeSyncEvent {
        return recordLikeSyncEventJpaRepository.findById(id).orElseThrow {
            DataNotFoundException(errorCode = ErrorCode.RECORD_LIKE_SYNC_EVENT_NOT_FOUND)
        }
    }

    override fun findByIdOrNull(id: Long): RecordLikeSyncEvent? {
        return recordLikeSyncEventJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByStatus(status: RecordLikeSyncEventStatus): List<RecordLikeSyncEvent> {
        return recordLikeSyncEventJpaRepository.findAllByStatus(status = status)
    }

    override fun findAllByStatus(status: RecordLikeSyncEventStatus, paging: Paging): List<RecordLikeSyncEvent> {
        val pageable = PageRequest.of(
            (paging.page - 1).toInt(),
            paging.size.toInt(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return recordLikeSyncEventJpaRepository.findAllByStatus(status = status, pageable = pageable)
    }

    override fun countByStatus(status: RecordLikeSyncEventStatus): Long {
        return recordLikeSyncEventJpaRepository.countByStatus(status = status)
    }

    override fun findAllByStatusOrderByIdAsc(status: RecordLikeSyncEventStatus, limit: Long): List<RecordLikeSyncEvent> {
        val pageable = PageRequest.of(0, limit.toInt())
        return recordLikeSyncEventJpaRepository.findAllByStatusOrderByIdAsc(status = status, pageable = pageable)
    }
}
