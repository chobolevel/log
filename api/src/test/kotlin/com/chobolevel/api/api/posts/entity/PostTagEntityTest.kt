package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostTag
import com.chobolevel.api.common.dummy.tags.DummyTag
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("게시글 태그 엔티티 단위 테스트")
class PostTagEntityTest {

    @Test
    @DisplayName("게시글 태그에 게시글을 매핑하면 게시글의 postTags 목록에도 추가된다")
    fun `게시글_태그_게시글_양방향_매핑`() {
        // given
        val post = DummyPost.toEntity()
        val postTag = DummyPostTag.toEntity()

        // when
        // PostTag.setBy(post)는 양방향 연관관계 편의 메서드다.
        // - postTag.post = post 설정
        // - post.addPostTag(postTag) 호출하여 역방향도 함께 설정
        postTag.setBy(post)

        // then
        postTag.post shouldBe post
        post.postTags shouldHaveSize 1
        post.postTags.first() shouldBe postTag
    }

    @Test
    @DisplayName("게시글 태그에 태그를 매핑하면 태그의 postTags 목록에도 추가된다")
    fun `게시글_태그_태그_양방향_매핑`() {
        // given
        val tag = DummyTag.toEntity()
        val postTag = DummyPostTag.toEntity()

        // when
        // PostTag.setBy(tag)도 마찬가지로 양방향 편의 메서드다.
        postTag.setBy(tag)

        // then
        postTag.tag shouldBe tag
        tag.postTags shouldHaveSize 1
        tag.postTags.first() shouldBe postTag
    }

    @Test
    @DisplayName("게시글과 태그를 모두 매핑한다")
    fun `게시글_태그_전체_매핑`() {
        // given
        val post = DummyPost.toEntity()
        val tag = DummyTag.toEntity()
        val postTag = DummyPostTag.toEntity()

        // when
        postTag.setBy(post)
        postTag.setBy(tag)

        // then: PostTag가 Post와 Tag를 모두 올바르게 참조해야 한다.
        postTag.post shouldBe post
        postTag.tag shouldBe tag
    }
}
