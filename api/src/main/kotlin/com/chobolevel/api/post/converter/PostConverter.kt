package com.chobolevel.api.post.converter

import com.chobolevel.api.post.dto.CreatePostRequest
import com.chobolevel.api.post.dto.PostResponse
import com.chobolevel.api.post.dto.SearchPostRequest
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.vo.PostQueryFilter
import org.springframework.stereotype.Component

@Component
class PostConverter(
    private val userConverter: UserConverter,
    private val tagConverter: TagConverter,
    private val postImageConverter: PostImageConverter,
) {

    fun convert(request: CreatePostRequest): Post {
        return Post(
            title = request.title,
            subTitle = request.subTitle,
            content = request.content
        )
    }

    fun convert(request: SearchPostRequest): PostQueryFilter {
        return PostQueryFilter(
            tagId = request.tagId,
            title = request.title,
            subTitle = request.subTitle,
            userId = request.userId
        )
    }

    fun convert(entity: Post): PostResponse {
        val convertedThumbNailImage = if (entity.getThumbNailImage() != null) {
            postImageConverter.convert(entity.getThumbNailImage()!!)
        } else {
            null
        }
        return PostResponse(
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

    fun convert(entities: List<Post>): List<PostResponse> {
        return entities.map { convert(it) }
    }
}
