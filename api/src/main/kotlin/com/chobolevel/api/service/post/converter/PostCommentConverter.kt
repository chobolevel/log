package com.chobolevel.api.service.post.converter

import com.chobolevel.api.dto.post.comment.CreatePostCommentRequestDto
import com.chobolevel.api.dto.post.comment.PostCommentResponseDto
import com.chobolevel.domain.entity.post.comment.PostComment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PostCommentConverter(
    private val passwordEncoder: BCryptPasswordEncoder
) {

    fun convert(request: CreatePostCommentRequestDto): PostComment {
        return PostComment(
            writerName = request.writerName,
            password = passwordEncoder.encode(request.password),
            content = request.content,
        )
    }

    fun convert(entity: PostComment): PostCommentResponseDto {
        return PostCommentResponseDto(
            id = entity.id!!,
            writerName = entity.writerName,
            content = entity.content,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
