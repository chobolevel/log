package com.chobolevel.domain.record.vo

data class RecordQueryFilter(
    val userId: Long?,
    val type: RecordType?,
    val title: String?,
    val tagName: String?,
)
