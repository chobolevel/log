package com.chobolevel.api.post.image.converter

import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.api.post.image.dto.PostImageResponse
import com.chobolevel.domain.post.image.entity.PostImage
import org.springframework.stereotype.Component

@Component
class PostImageConverter {

    fun convert(request: CreatePostImageRequest): PostImage {
        return PostImage(
            type = request.type,
            name = request.name,
            url = request.url,
            width = request.width ?: 0,
            height = request.height ?: 0
        )
    }

    fun convert(entity: PostImage): PostImageResponse {
        return PostImageResponse(
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
