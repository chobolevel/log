package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostImage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("게시글 이미지 엔티티 레이어 테스트")
class PostImageEntityTest {

    @Test
    fun 게시글_이미지_게시글_매핑() {
        // given
        val dummyPostImage = DummyPostImage.toEntity()
        val dummyPost = DummyPost.toEntity()

        // when
        dummyPostImage.setBy(dummyPost)

        // then
        assertThat(dummyPostImage.post).isEqualTo(dummyPost)
    }

    @Test
    fun 게시글_이미지_삭제() {
        // given
        val dummyPostImage = DummyPostImage.toEntity()

        // when
        dummyPostImage.delete()

        // then
        assertThat(dummyPostImage.deleted).isTrue()
    }
}
