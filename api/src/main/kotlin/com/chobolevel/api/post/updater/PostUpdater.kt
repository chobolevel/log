package com.chobolevel.api.post.updater

import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.tag.repository.PostTagRepository
import com.chobolevel.domain.post.vo.PostUpdateMask
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import org.springframework.stereotype.Component

@Component
class PostUpdater(
    private val postTagRepository: PostTagRepository,
    private val tagRepository: TagRepository,
    private val postImageConverter: PostImageConverter,
) : PostUpdatable {

    override fun markAsUpdate(request: UpdatePostRequest, entity: Post): Post {
        request.updateMask.forEach {
            when (it) {
                PostUpdateMask.TAGS -> {
                    postTagRepository.deleteAllInBatch(entity.postTags)
                    val tags: List<Tag> = tagRepository.findByIds(request.tagIds!!)
                    entity.addTags(tags)
                }
                PostUpdateMask.TITLE -> entity.title = request.title!!
                PostUpdateMask.SUB_TITLE -> entity.subTitle = request.subTitle!!
                PostUpdateMask.CONTENT -> entity.content = request.content!!
                PostUpdateMask.THUMB_NAIL_IMAGE -> {
                    // 수정/삭제 구분하도록 변경 필요
                    entity.getThumbnailImage()?.let { postThumbnailImage ->
                        entity.removePostImage(postImage = postThumbnailImage)
                    }
                    request.thumbnailImage?.let { createPostImageRequest ->
                        val postThumbnailImage: PostImage = postImageConverter.convert(request = createPostImageRequest)
                        entity.addPostImage(postThumbnailImage)
                    }
                }
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
