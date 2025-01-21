package com.chobolevel.api.service.post.converter

import com.chobolevel.api.dto.post.comment.CreatePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.PostCommentResponseDto
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.domain.entity.post.comment.PostComment
import org.springframework.stereotype.Component

@Component
class PostCommentConverter(
    private val userConverter: UserConverter
) {

    fun convert(request: CreatePostCommentRequestDto): PostComment {
        return PostComment(
            content = request.content,
        )
    }

    fun convert(entity: PostComment): PostCommentResponseDto {
        return PostCommentResponseDto(
            id = entity.id!!,
            writer = userConverter.convert(entity.writer!!),
            content = entity.content,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
