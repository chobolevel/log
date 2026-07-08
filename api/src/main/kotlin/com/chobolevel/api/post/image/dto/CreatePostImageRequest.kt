package com.chobolevel.api.post.image.dto

import com.chobolevel.domain.post.image.vo.PostImageType
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreatePostImageRequest(
    @field:NotNull(message = "이미지 타입은 필수 값입니다.")
    val type: PostImageType,
    @field:NotEmpty(message = "이미지 이름은 필수 값입니다.")
    val name: String,
    @field:NotEmpty(message = "이미지 경로는 필수 값입니다.")
    val url: String,
    val width: Int?,
    val height: Int?,
)
