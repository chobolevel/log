package com.chobolevel.infrastructure.record.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.exception.RecordErrorCode
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordFeedQueryFilter
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RecordRepositoryAdapter(
    private val recordJpaRepository: RecordJpaRepository,
    private val recordQuerydslRepository: RecordQuerydslRepository
) : RecordRepository {

    override fun save(record: Record): Record {
        return recordJpaRepository.save(record)
    }

    override fun findById(id: Long): Record {
        return recordJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw BusinessException(
            errorCode = RecordErrorCode.RECORD_NOT_FOUND
        )
    }

    override fun existsById(id: Long): Boolean {
        return recordJpaRepository.existsByIdAndIsDeletedFalse(id)
    }

    override fun searchRecords(
        queryFilter: RecordQueryFilter,
        paging: Paging,
        orderTypes: List<RecordOrderType>
    ): List<Record> {
        return recordQuerydslRepository.searchByFilter(
            filter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
    }

    override fun searchRecordsCount(queryFilter: RecordQueryFilter): Long {
        return recordQuerydslRepository.countByFilter(filter = queryFilter)
    }

    override fun searchRecordFeeds(queryFilter: RecordFeedQueryFilter, limit: Long): List<Record> {
        return recordQuerydslRepository.searchFeedByFilter(filter = queryFilter, limit = limit)
    }

    override fun findCreatedAtsByUserIdAndCreatedAtBetween(userId: Long, start: OffsetDateTime, end: OffsetDateTime): List<OffsetDateTime> {
        return recordQuerydslRepository.findCreatedAtsByUserIdAndCreatedAtBetween(userId = userId, start = start, end = end)
    }
}
