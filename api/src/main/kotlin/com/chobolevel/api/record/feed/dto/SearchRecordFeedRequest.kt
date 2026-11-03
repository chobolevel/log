package com.chobolevel.api.record.feed.dto

data class SearchRecordFeedRequest(
    val cursor: String? = null,
    val size: Long = 20,
)
