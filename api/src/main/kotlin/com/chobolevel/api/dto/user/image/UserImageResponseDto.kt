package com.chobolevel.api.dto.user.image

import com.chobolevel.domain.entity.user.image.UserImageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserImageResponseDto(
    val id: Long,
    val type: UserImageType,
    val originUrl: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)
