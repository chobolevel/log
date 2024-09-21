package com.chobolevel.api.service.post.updater

import com.chobolevel.api.dto.post.UpdatePostRequestDto
import com.chobolevel.api.service.post.converter.PostImageConverter
import com.chobolevel.domain.entity.post.Post
import com.chobolevel.domain.entity.post.PostUpdateMask
import com.chobolevel.domain.entity.post.tag.PostTag
import com.chobolevel.domain.entity.post.tag.PostTagRepository
import com.chobolevel.domain.entity.tag.TagFinder
import org.springframework.stereotype.Component

@Component
class PostUpdater(
    private val postTagRepository: PostTagRepository,
    private val tagFinder: TagFinder,
    private val postImageConverter: PostImageConverter,
) : PostUpdatable {

    override fun markAsUpdate(request: UpdatePostRequestDto, entity: Post): Post {
        request.updateMask.forEach {
            when (it) {
                PostUpdateMask.TAGS -> {
                    postTagRepository.deleteAllInBatch(entity.postTags)
                    request.tagIds!!.forEach { tagId ->
                        val tag = tagFinder.findById(tagId)
                        val postTag = PostTag()
                        postTag.setBy(tag)
                        postTag.setBy(entity)
                    }
                }

                PostUpdateMask.TITLE -> entity.title = request.title!!
                PostUpdateMask.SUB_TITLE -> entity.subTitle = request.subTitle!!
                PostUpdateMask.CONTENT -> entity.content = request.content!!
                PostUpdateMask.THUMB_NAIL_IMAGE -> {
                    entity.deleteThumbNailImage()
                    if (request.thumbNailImage != null) {
                        postImageConverter.convert(request.thumbNailImage).also { postImage ->
                            postImage.setBy(entity)
                        }
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
