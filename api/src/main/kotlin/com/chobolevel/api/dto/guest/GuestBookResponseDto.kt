package com.chobolevel.api.dto.guest

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GuestBookResponseDto(
    val id: Long,
    val guestName: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
