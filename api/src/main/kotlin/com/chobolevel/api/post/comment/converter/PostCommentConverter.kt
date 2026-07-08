package com.chobolevel.api.post.comment.converter

import com.chobolevel.api.post.comment.dto.CreatePostCommentRequest
import com.chobolevel.api.post.comment.dto.PostCommentResponse
import com.chobolevel.api.post.comment.dto.SearchPostCommentRequest
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
import org.springframework.stereotype.Component

@Component
class PostCommentConverter(
    private val userConverter: UserConverter
) {

    fun convert(request: CreatePostCommentRequest): PostComment {
        return PostComment(
            content = request.content,
        )
    }

    fun convert(request: SearchPostCommentRequest): PostCommentQueryFilter {
        return PostCommentQueryFilter(
            postId = request.postId,
            writerId = request.writerId
        )
    }

    fun convert(entity: PostComment): PostCommentResponse {
        return PostCommentResponse(
            id = entity.id!!,
            writer = userConverter.convert(entity.writer!!),
            content = entity.content,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<PostComment>): List<PostCommentResponse> {
        return entities.map { convert(it) }
    }
}
