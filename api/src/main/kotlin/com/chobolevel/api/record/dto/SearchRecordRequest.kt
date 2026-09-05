package com.chobolevel.api.record.dto

import com.chobolevel.domain.record.vo.RecordType

data class SearchRecordRequest(
    val userId: Long?,
    val type: RecordType?,
    val title: String?
)
