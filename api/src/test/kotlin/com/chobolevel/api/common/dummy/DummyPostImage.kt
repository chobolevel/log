package com.chobolevel.api.common.dummy

import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.image.vo.PostImageType

object DummyPostImage {
    val ID: Long = 1L
    val TYPE: PostImageType = PostImageType.THUMBNAIL
    val NAME: String = "newjeans-docker.jpg"
    val PATH: String = "/image/2024/10/12/ca022577-82b7-49b7-be29-c557e217b5e8.png"
    val WIDTH: Int = 100
    val HEIGHT: Int = 100

    fun toEntity(): PostImage = PostImage(
        type = TYPE,
        name = NAME,
        path = PATH,
        width = WIDTH,
        height = HEIGHT
    ).also { it.id = ID }

    fun toCreateRequest(): CreatePostImageRequest = CreatePostImageRequest(
        type = TYPE,
        name = NAME,
        path = PATH,
        width = WIDTH,
        height = HEIGHT
    )
}
