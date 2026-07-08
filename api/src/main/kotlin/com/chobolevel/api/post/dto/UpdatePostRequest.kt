package com.chobolevel.api.post.dto

import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.domain.post.vo.PostUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdatePostRequest(
    val tagIds: List<Long>?,
    val title: String?,
    val subTitle: String?,
    val content: String?,
    val thumbNailImage: CreatePostImageRequest?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostUpdateMask>
)
