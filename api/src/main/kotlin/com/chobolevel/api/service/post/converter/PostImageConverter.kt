package com.chobolevel.api.service.post.converter

import com.chobolevel.api.dto.post.image.CreatePostImageRequestDto
import com.chobolevel.api.dto.post.image.PostImageResponseDto
import com.chobolevel.domain.entity.post.image.PostImage
import org.springframework.stereotype.Component

@Component
class PostImageConverter {

    fun convert(request: CreatePostImageRequestDto): PostImage {
        return PostImage(
            type = request.type,
            name = request.name,
            url = request.url,
            width = request.width ?: 0,
            height = request.height ?: 0
        )
    }

    fun convert(entity: PostImage): PostImageResponseDto {
        return PostImageResponseDto(
            id = entity.id!!,
            type = entity.type,
            name = entity.name,
            url = entity.url,
            width = entity.width,
            height = entity.height,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
