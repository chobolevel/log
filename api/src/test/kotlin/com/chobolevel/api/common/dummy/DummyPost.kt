package com.chobolevel.api.common.dummy

import com.chobolevel.domain.post.entity.Post

object DummyPost {
    val ID: Long = 1L
    val TITLE: String = "testPostTitle"
    val SUB_TITLE: String = "testPostSubTitle"
    val CONTENT: String = "testPostContent"

    fun toEntity(): Post = Post(
        title = TITLE,
        subTitle = SUB_TITLE,
        content = CONTENT
    ).also { it.id = ID }
}
