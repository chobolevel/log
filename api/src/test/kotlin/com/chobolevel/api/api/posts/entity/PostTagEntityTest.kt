package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostTag
import com.chobolevel.api.common.dummy.tags.DummyTag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("게시글 태그 엔티티 레이어 테스트")
class PostTagEntityTest {

    @Test
    fun 게시글_태그_게시글_매핑() {
        // given
        val dummyPostTag = DummyPostTag.toEntity()
        val dummyPost = DummyPost.toEntity()

        // when
        dummyPostTag.setBy(dummyPost)

        // then
        assertThat(dummyPostTag.post).isEqualTo(dummyPost)
    }

    @Test
    fun 게시글_태그_태그_매핑() {
        // given
        val dummyPostTag = DummyPostTag.toEntity()
        val dummyTag = DummyTag.toEntity()

        // when
        dummyPostTag.setBy(dummyTag)

        // then
        assertThat(dummyPostTag.tag).isEqualTo(dummyTag)
    }
}
