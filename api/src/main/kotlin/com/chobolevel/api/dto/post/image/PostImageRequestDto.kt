package com.chobolevel.api.dto.post.image

import com.chobolevel.domain.entity.post.image.PostImageType
import com.chobolevel.domain.entity.post.image.PostImageUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreatePostImageRequestDto(
    @field:NotNull(message = "이미지 타입은 필수 값입니다.")
    val type: PostImageType,
    @field:NotEmpty(message = "이미지 이름은 필수 값입니다.")
    val name: String,
    @field:NotEmpty(message = "이미지 경로는 필수 값입니다.")
    val url: String,
    val width: Int?,
    val height: Int?,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdatePostImageRequestDto(
    val type: PostImageType?,
    val name: String?,
    val url: String?,
    val width: Int?,
    val height: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostImageUpdateMask>
)
