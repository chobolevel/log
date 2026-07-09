package com.chobolevel.api.post.image.dto

import com.chobolevel.domain.post.image.vo.PostImageType
import com.chobolevel.domain.post.image.vo.PostImageUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdatePostImageRequest(
    val type: PostImageType?,
    val name: String?,
    val path: String?,
    val width: Int?,
    val height: Int?,
    @field:Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostImageUpdateMask>
)
