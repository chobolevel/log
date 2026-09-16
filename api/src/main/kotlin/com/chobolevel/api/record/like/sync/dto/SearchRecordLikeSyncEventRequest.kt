package com.chobolevel.api.record.like.sync.dto

data class SearchRecordLikeSyncEventRequest(
    val page: Long = 1,
    val size: Long = 100
)
