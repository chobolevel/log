package com.chobolevel.api.record.dto

import com.chobolevel.domain.record.vo.RecordOrderType
import com.chobolevel.domain.record.vo.RecordType

data class SearchRecordRequest(
    val userId: Long?,
    val type: RecordType?,
    val title: String?,
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<RecordOrderType> = emptyList()
)
