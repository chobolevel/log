package com.chobolevel.api.common.dummy.posts

import com.chobolevel.api.post.image.dto.PostImageResponseDto
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.image.entity.PostImageType
import java.time.OffsetDateTime

object DummyPostImage {
    val ID = 1L
    val TYPE = PostImageType.THUMB_NAIL
    const val NAME = "게시글 썸네일"
    const val URL = "https://chobolevel.s3.ap-northeast-2.amazonaws.com/image/2024/08/30/93c5e6d9-a261-4179-967c-c032e859194e.jpg"
    const val WIDTH = 100
    const val HEIGHT = 100

    // PostImage도 가변 객체(post 필드, deleted 필드)이므로 매번 새 인스턴스를 반환한다.
    fun toEntity(): PostImage {
        return PostImage(
            type = TYPE,
            name = NAME,
            url = URL,
            width = WIDTH,
            height = HEIGHT
        ).also {
            it.id = ID
            it.createdAt = OffsetDateTime.now()
            it.updatedAt = OffsetDateTime.now()
        }
    }

    fun toResponseDto(): PostImageResponseDto {
        return PostImageResponseDto(
            id = ID,
            type = TYPE,
            name = NAME,
            url = URL,
            width = WIDTH,
            height = HEIGHT,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
