package com.chobolevel.api.post.comment.updater

import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequest
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.vo.PostCommentUpdateMask
import org.springframework.stereotype.Component

@Component
class PostCommentUpdater : PostCommentUpdatable {

    override fun markAsUpdate(request: UpdatePostCommentRequest, entity: PostComment): PostComment {
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
