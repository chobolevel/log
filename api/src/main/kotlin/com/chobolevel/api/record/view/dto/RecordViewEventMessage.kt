package com.chobolevel.api.record.view.dto

data class RecordViewEventMessage(
    val recordId: Long,
    val userId: Long?,
    val guestId: String?,
)
