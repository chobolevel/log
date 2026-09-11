package com.chobolevel.api.post.dto

import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.domain.post.vo.PostUpdateMask
import jakarta.validation.constraints.Size

data class UpdatePostRequest(
    val tagIds: List<Long>?,
    val title: String?,
    val subTitle: String?,
    val content: String?,
    val thumbnailImage: CreatePostImageRequest?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostUpdateMask>
)
