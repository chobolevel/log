package com.chobolevel.api.dto.post.comment

import com.chobolevel.domain.entity.post.comment.PostCommentUpdateMask
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreatePostCommentRequestDto(
    @field:NotNull(message = "게시글 아이디는 필수 값입니다.")
    val postId: Long,
    @field:NotEmpty(message = "댓글 내용은 필수 값입니다.")
    val content: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UpdatePostCommentRequestDto(
    val content: String?,
    @Size(min = 1, message = "update_mask는 필수 값입니다.")
    val updateMask: List<PostCommentUpdateMask>
)
