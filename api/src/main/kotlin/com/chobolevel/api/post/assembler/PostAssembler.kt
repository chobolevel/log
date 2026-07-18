package com.chobolevel.api.post.assembler

import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.user.entity.User
import org.springframework.stereotype.Component

@Component
class PostAssembler {

    fun assemble(
        post: Post,
        postThumbnailImage: PostImage?,
        user: User,
        tags: List<Tag>
    ): Post {
        postThumbnailImage?.let { post.addPostImage(postImage = postThumbnailImage) }
        post.assignWriter(user = user)
        post.addTags(tags = tags)
        return post
    }
}
