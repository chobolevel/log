package com.chobolevel.api.common.web
data class PagingResponse<T>(
    val page: Long,
    val size: Long,
    val data: List<T>,
    val totalCount: Long,
)
