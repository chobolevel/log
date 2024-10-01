package com.chobolevel.api.service.post.updater

import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.domain.entity.post.comment.PostComment
import com.chobolevel.domain.entity.post.comment.PostCommentUpdateMask
import org.springframework.stereotype.Component

@Component
class PostCommentUpdater : PostCommentUpdatable {

    override fun markAsUpdate(request: UpdatePostCommentRequestDto, entity: PostComment): PostComment {
        request.updateMask.forEach {
            when (it) {
                PostCommentUpdateMask.CONTENT -> entity.content = request.content!!
                PostCommentUpdateMask.DELETE -> entity.deleted = true
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
