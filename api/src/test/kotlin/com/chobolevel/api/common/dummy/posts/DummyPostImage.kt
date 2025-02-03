package com.chobolevel.api.common.dummy.posts

import com.chobolevel.domain.entity.post.image.PostImage
import com.chobolevel.domain.entity.post.image.PostImageType

object DummyPostImage {
    private val id = 1L
    private val type = PostImageType.THUMB_NAIL
    private val name = "게시글 사진"
    private val url =
        "https://chobolevel.s3.ap-northeast-2.amazonaws.com/image/2024/08/30/93c5e6d9-a261-4179-967c-c032e859194e.jpg"
    private val width = 100
    private val height = 100

    fun toEntity(): PostImage {
        return postImage
    }

    private val postImage: PostImage by lazy {
        PostImage(
            type = type,
            name = name,
            url = url,
            width = width,
            height = height,
        ).also {
            it.id = id
        }
    }
}
