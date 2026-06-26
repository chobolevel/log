package com.chobolevel.api.post.updater

import com.chobolevel.api.post.dto.UpdatePostCommentRequestDto
import com.chobolevel.domain.post.comment.PostComment
import com.chobolevel.domain.post.comment.PostCommentUpdateMask
import org.springframework.stereotype.Component

@Component
class PostCommentUpdater : PostCommentUpdatable {

    override fun markAsUpdate(request: UpdatePostCommentRequestDto, entity: PostComment): PostComment {
        request.updateMask.forEach {
            when (it) {
                PostCommentUpdateMask.CONTENT -> entity.content = request.content!!
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
