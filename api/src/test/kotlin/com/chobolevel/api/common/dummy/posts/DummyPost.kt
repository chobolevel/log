package com.chobolevel.api.common.dummy.posts

import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.dto.post.CreatePostRequestDto
import com.chobolevel.domain.entity.post.Post

object DummyPost {
    private val id = 1L
    private val title = "JAVA의 정석"
    private val subTitle = "JAVA의 정석을 읽으며"
    private val content = "JAVA의 정석은 다 자바"

    fun toEntity(): Post {
        return post
    }

    private val post: Post by lazy {
        Post(
            title = title,
            subTitle = subTitle,
            content = content
        ).also {
            it.id = id
        }
    }
}
