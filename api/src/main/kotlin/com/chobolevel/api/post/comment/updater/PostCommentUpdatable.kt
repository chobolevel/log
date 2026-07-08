package com.chobolevel.api.post.comment.updater

import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequest
import com.chobolevel.domain.post.comment.entity.PostComment

interface PostCommentUpdatable {

    fun markAsUpdate(request: UpdatePostCommentRequest, entity: PostComment): PostComment

    fun order(): Int
}
