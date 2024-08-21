package com.chobolevel.api.dto.post

import com.chobolevel.domain.entity.post.tag.PostTagUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreatePostTagRequestDto(
    @field:NotEmpty(message = "태그 이름은 필수 값입니다.")
    val name: String,
    @field:NotNull(message = "태그 순서는 필수 값입니다.")
    val order: Int,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdatePostTagRequestDto(
    val name: String?,
    val order: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostTagUpdateMask>
)
