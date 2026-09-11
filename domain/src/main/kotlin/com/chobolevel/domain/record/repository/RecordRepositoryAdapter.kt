package com.chobolevel.domain.record.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.record.entity.QRecord.record
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.stereotype.Component

@Component
class RecordRepositoryAdapter(
    private val recordJpaRepository: RecordJpaRepository,
    private val recordQuerydslRepository: RecordQuerydslRepository
) : RecordRepository {

    override fun save(record: Record): Record {
        return recordJpaRepository.save(record)
    }

    override fun findById(id: Long): Record {
        return recordJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.RECORD_NOT_FOUND
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
        return recordQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchRecordsCount(queryFilter: RecordQueryFilter): Long {
        return recordQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<RecordOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                RecordOrderType.CREATED_AT_ASC -> record.createdAt.asc()
                RecordOrderType.CREATED_AT_DESC -> record.createdAt.desc()
                RecordOrderType.UPDATED_AT_ASC -> record.updatedAt.asc()
                RecordOrderType.UPDATED_AT_DESC -> record.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
