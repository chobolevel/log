package com.chobolevel.api.tag.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateTagRequest(
    @field:NotEmpty(message = "태그 이름은 필수 값입니다.")
    val name: String,
    @field:NotNull(message = "태그 순서는 필수 값입니다.")
    val order: Int,
)
