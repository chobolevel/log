package com.chobolevel.api.dto.client

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ClientResponseDto(
    val clientId: String,
    val clientSecret: String,
    val name: String,
    val redirectUrl: String,
    val createdAt: Long,
    val updatedAt: Long,
)
