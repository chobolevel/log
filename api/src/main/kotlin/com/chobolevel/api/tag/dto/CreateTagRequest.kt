package com.chobolevel.api.tag.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreateTagRequest(
    @field:NotEmpty(message = "태그 이름은 필수 값입니다.")
    val name: String,
    @field:NotNull(message = "태그 순서는 필수 값입니다.")
    val order: Int,
)
