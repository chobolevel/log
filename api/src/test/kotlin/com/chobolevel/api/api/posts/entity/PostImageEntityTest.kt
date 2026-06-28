package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostImage
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("게시글 이미지 엔티티 단위 테스트")
class PostImageEntityTest {

    @Test
    @DisplayName("게시글 이미지에 게시글을 매핑하면 게시글의 이미지 목록에도 추가된다")
    fun `게시글_이미지_게시글_양방향_매핑`() {
        // given
        val post = DummyPost.toEntity()
        val postImage = DummyPostImage.toEntity()

        // when
        // PostImage.setBy(post)는 양방향 연관관계 편의 메서드다.
        // - postImage.post = post 설정
        // - post.addImage(postImage) 호출하여 역방향도 함께 설정
        postImage.setBy(post)

        // then: 양방향 모두 검증
        postImage.post shouldBe post
        post.images shouldHaveSize 1
        post.images.first() shouldBe postImage
    }

    @Test
    @DisplayName("이미 동일한 게시글이 설정되어 있으면 재설정해도 post 참조는 변하지 않는다")
    fun `게시글_이미지_동일_게시글_재설정_무시`() {
        // given
        val post = DummyPost.toEntity()
        val postImage = DummyPostImage.toEntity()
        postImage.setBy(post)

        // when: 같은 post를 다시 설정
        postImage.setBy(post)

        // then: images에 중복 추가 없이 1개 유지 (addImage의 contains 체크)
        post.images shouldHaveSize 1
    }

    @Test
    @DisplayName("게시글 이미지를 소프트 딜리트 처리한다")
    fun `게시글_이미지_삭제`() {
        // given
        val postImage = DummyPostImage.toEntity()

        // when
        postImage.delete()

        // then
        postImage.deleted shouldBe true
    }
}
