package com.chobolevel.api.service.post.converter

import com.chobolevel.api.dto.post.CreatePostTagRequestDto
import com.chobolevel.api.dto.post.PostTagResponseDto
import com.chobolevel.domain.entity.post.tag.PostTag
import org.springframework.stereotype.Component

@Component
class PostTagConverter {

    fun convert(request: CreatePostTagRequestDto): PostTag {
        return PostTag(
            name = request.name,
            order = request.order
        )
    }

    fun convert(entity: PostTag): PostTagResponseDto {
        return PostTagResponseDto(
            id = entity.id!!,
            name = entity.name,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }
}
