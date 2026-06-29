package com.chobolevel.api.post.comment.updater

import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequestDto
import com.chobolevel.domain.post.comment.entity.PostComment

interface PostCommentUpdatable {

    fun markAsUpdate(request: UpdatePostCommentRequestDto, entity: PostComment): PostComment

    fun order(): Int
}
