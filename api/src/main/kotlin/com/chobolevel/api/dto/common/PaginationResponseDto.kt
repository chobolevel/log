package com.chobolevel.api.dto.common

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PaginationResponseDto(
    val totalCount: Long,
    val skipCount: Long,
    val limitCount: Long,
    val data: List<Any>
)
