package com.chobolevel.infrastructure.record.view.repository

import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import org.springframework.stereotype.Component

@Component
class RecordViewRepositoryAdapter(
    private val recordViewJpaRepository: RecordViewJpaRepository,
) : RecordViewRepository {

    override fun save(recordView: RecordView): RecordView {
        return recordViewJpaRepository.save(recordView)
    }

    override fun countByRecordId(recordId: Long): Long {
        return recordViewJpaRepository.countByRecordId(recordId = recordId)
    }
}
