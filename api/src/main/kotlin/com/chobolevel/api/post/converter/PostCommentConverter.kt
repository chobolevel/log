package com.chobolevel.api.post.converter

import com.chobolevel.api.post.dto.CreatePostCommentRequestDto
import com.chobolevel.api.post.dto.PostCommentResponseDto
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.post.comment.PostComment
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

    fun convert(entities: List<PostComment>): List<PostCommentResponseDto> {
        return entities.map { convert(it) }
    }
}
