package com.chobolevel.api.post.dto

import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreatePostRequest(
    @field:Size(min = 1, message = "게시글 태그는 필수 값입니다.")
    val tagIds: List<Long>,
    @field:NotEmpty(message = "게시글 제목은 필수 값입니다.")
    val title: String,
    @field:NotEmpty(message = "게시글 부제목은 필수 값입니다.")
    var subTitle: String,
    @field:NotEmpty(message = "게시글 내용은 필수 값입니다.")
    val content: String,
    val thumbnailImage: CreatePostImageRequest?
)
