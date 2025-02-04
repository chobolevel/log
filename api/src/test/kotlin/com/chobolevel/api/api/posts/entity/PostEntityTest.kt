package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostImage
import com.chobolevel.api.common.dummy.posts.DummyPostTag
import com.chobolevel.api.common.dummy.tags.DummyTag
import com.chobolevel.api.common.dummy.users.DummyUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("게시글 엔티티 레이어 테스트")
class PostEntityTest {

    @Test
    fun 게시글_회원_매핑() {
        // given
        val dummyUser = DummyUser.toEntity()
        val dummyPost = DummyPost.toEntity()

        // when
        dummyPost.setBy(dummyUser)

        // then
        assertThat(dummyPost.user).isEqualTo(dummyUser)
    }

    @Test
    fun 게시글_게시글_태그_매핑() {
        // given
        val dummyPost = DummyPost.toEntity()
        val dummyTag = DummyTag.toEntity()
        val dummyPostTag = DummyPostTag.toEntity()
        dummyPostTag.setBy(dummyPost)
        dummyPostTag.setBy(dummyTag)

        // when
        dummyPost.addPostTag(dummyPostTag)

        // then
        val firstPostTag = dummyPost.postTags.first()
        assertThat(firstPostTag).isEqualTo(dummyPostTag)
        assertThat(firstPostTag.tag).isEqualTo(dummyTag)
        assertThat(firstPostTag.post).isEqualTo(dummyPost)
    }

    @Test
    fun 게시글_게시글_이미지_매핑() {
        // given
        val dummyPost = DummyPost.toEntity()
        val dummyPostImage = DummyPostImage.toEntity()

        // when
        dummyPost.addImage(dummyPostImage)

        // then
        assertThat(dummyPost.images).isNotEmpty()
        assertThat(dummyPost.images.first()).isEqualTo(dummyPostImage)
    }
}
