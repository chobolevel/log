package com.chobolevel.api.common.dto


data class PagingResponse<T>(
    val page: Long,
    val size: Long,
    val data: List<T>,
    val totalCount: Long,
)
