package com.chobolevel.api.post.updater

import com.chobolevel.api.post.dto.UpdatePostCommentRequestDto
import com.chobolevel.domain.post.comment.PostComment

interface PostCommentUpdatable {

    fun markAsUpdate(request: UpdatePostCommentRequestDto, entity: PostComment): PostComment

    fun order(): Int
}
