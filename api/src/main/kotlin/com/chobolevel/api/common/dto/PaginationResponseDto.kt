package com.chobolevel.api.common.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PaginationResponseDto(
    val totalCount: Long,
    val skipCount: Long,
    val limitCount: Long,
    val data: List<Any>
)
