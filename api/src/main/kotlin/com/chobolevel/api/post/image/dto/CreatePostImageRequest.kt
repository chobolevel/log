package com.chobolevel.api.post.image.dto

import com.chobolevel.domain.post.image.vo.PostImageType
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CreatePostImageRequest(
    @field:NotNull(message = "이미지 타입은 필수 값입니다.")
    val type: PostImageType,
    @field:NotEmpty(message = "이미지 이름은 필수 값입니다.")
    val name: String,
    @field:NotEmpty(message = "이미지 경로는 필수 값입니다.")
    val path: String,
    val width: Int?,
    val height: Int?,
)
