package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostComment
import com.chobolevel.api.common.dummy.users.DummyUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("게시글 댓글 엔티티 레이어 테스트")
class PostCommentEntityTest {

    @Test
    fun 게시글_댓글_게시글_매핑() {
        // given
        val dummyPostComment = DummyPostComment.toEntity()
        val dummyPost = DummyPost.toEntity()

        // when
        dummyPostComment.setBy(dummyPost)

        // then
        assertThat(dummyPostComment.post).isEqualTo(dummyPost)
    }

    @Test
    fun 게시글_댓글_회원_매핑() {
        // given
        val dummyPostComment = DummyPostComment.toEntity()
        val dummyUser = DummyUser.toEntity()

        // when
        dummyPostComment.setBy(dummyUser)

        // then
        assertThat(dummyPostComment.writer).isEqualTo(dummyUser)
    }

    @Test
    fun 게시글_댓글_삭제() {
        // given
        val dummyPostComment = DummyPostComment.toEntity()

        // when
        dummyPostComment.delete()

        // then
        assertThat(dummyPostComment.deleted).isTrue()
    }
}
