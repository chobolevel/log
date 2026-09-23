package com.chobolevel.api.record.view.sync.dto

data class SearchRecordViewSyncEventRequest(
    val page: Long = 1,
    val size: Long = 100
)
