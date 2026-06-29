package com.chobolevel.api.common.dummy.posts

import com.chobolevel.domain.post.tag.entity.PostTag

object DummyPostTag {
    val ID = 1L

    // PostTag는 post, tag 필드를 가지므로 가변 객체다. 매번 새 인스턴스를 반환한다.
    fun toEntity(): PostTag {
        return PostTag().also {
            it.id = ID
        }
    }
}
