package com.chobolevel.api.common.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PagingResponse<T>(
    val page: Long,
    val size: Long,
    val data: List<T>,
    val totalCount: Long,
)
