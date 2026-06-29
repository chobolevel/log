package com.chobolevel.api.post.converter

import com.chobolevel.api.post.dto.CreatePostRequestDto
import com.chobolevel.api.post.dto.PostResponseDto
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.post.entity.Post
import org.springframework.stereotype.Component

@Component
class PostConverter(
    private val userConverter: UserConverter,
    private val tagConverter: TagConverter,
    private val postImageConverter: PostImageConverter,
) {

    fun convert(request: CreatePostRequestDto): Post {
        return Post(
            title = request.title,
            subTitle = request.subTitle,
            content = request.content
        )
    }

    fun convert(entity: Post): PostResponseDto {
        val convertedThumbNailImage = if (entity.getThumbNailImage() != null) {
            postImageConverter.convert(entity.getThumbNailImage()!!)
        } else {
            null
        }
        return PostResponseDto(
            id = entity.id!!,
            writer = userConverter.convert(entity.user!!),
            tags = entity.postTags.map { tagConverter.convert(it.tag!!) },
            title = entity.title,
            subTitle = entity.subTitle,
            content = entity.content,
            thumbNailImage = convertedThumbNailImage,
            createdAt = entity.createdAt!!.toInstant().toEpochMilli(),
            updatedAt = entity.updatedAt!!.toInstant().toEpochMilli()
        )
    }

    fun convert(entities: List<Post>): List<PostResponseDto> {
        return entities.map { convert(it) }
    }
}
