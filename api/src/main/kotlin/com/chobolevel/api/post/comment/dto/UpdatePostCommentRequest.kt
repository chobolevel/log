package com.chobolevel.api.post.comment.dto

import com.chobolevel.domain.post.comment.vo.PostCommentUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdatePostCommentRequest(
    val content: String?,
    @Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostCommentUpdateMask>
)
