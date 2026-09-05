package com.chobolevel.domain.record.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordQueryFilter

interface RecordRepository {

    fun save(record: Record): Record

    fun findById(id: Long): Record

    fun searchRecords(
        queryFilter: RecordQueryFilter,
        paging: Paging,
        orderTypes: List<RecordOrderType>
    ): List<Record>

    fun searchRecordsCount(queryFilter: RecordQueryFilter): Long
}
