package com.chobolevel.api.service.post.updater

import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.domain.entity.post.comment.PostComment

interface PostCommentUpdatable {

    fun markAsUpdate(request: UpdatePostCommentRequestDto, entity: PostComment): PostComment

    fun order(): Int
}
