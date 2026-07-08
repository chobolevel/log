package com.chobolevel.api.post.updater

import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.tag.entity.PostTag
import com.chobolevel.domain.post.tag.repository.PostTagRepository
import com.chobolevel.domain.post.vo.PostUpdateMask
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
                    request.tagIds!!.forEach { tagId ->
                        val tag = tagRepository.findById(tagId)
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
