package com.chobolevel.domain.record.like.sync.repository

import com.chobolevel.domain.record.like.sync.entity.RecordLikeSyncEvent
import com.chobolevel.domain.record.like.sync.vo.RecordLikeSyncEventStatus
import org.springframework.stereotype.Component

@Component
class RecordLikeSyncEventRepositoryAdapter(
    private val recordLikeSyncEventJpaRepository: RecordLikeSyncEventJpaRepository,
) : RecordLikeSyncEventRepository {

    override fun save(event: RecordLikeSyncEvent): RecordLikeSyncEvent {
        return recordLikeSyncEventJpaRepository.save(event)
    }

    override fun findByIdOrNull(id: Long): RecordLikeSyncEvent? {
        return recordLikeSyncEventJpaRepository.findById(id).orElse(null)
    }

    override fun findAllByStatus(status: RecordLikeSyncEventStatus): List<RecordLikeSyncEvent> {
        return recordLikeSyncEventJpaRepository.findAllByStatus(status = status)
    }
}
