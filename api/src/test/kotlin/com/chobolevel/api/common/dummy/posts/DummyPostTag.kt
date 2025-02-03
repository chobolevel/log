package com.chobolevel.api.common.dummy.posts

import com.chobolevel.domain.entity.post.tag.PostTag

object DummyPostTag {
    private val id = 1L

    fun toEntity(): PostTag {
        return postTag
    }

    private val postTag: PostTag by lazy {
        PostTag().also {
            it.id = id
        }
    }
}
