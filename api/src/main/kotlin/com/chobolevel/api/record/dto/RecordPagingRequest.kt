package com.chobolevel.api.record.dto

import com.chobolevel.domain.record.vo.RecordOrderType

data class RecordPagingRequest(
    val page: Long = 1,
    val size: Long = 20,
    val orderTypes: List<RecordOrderType> = emptyList()
)
