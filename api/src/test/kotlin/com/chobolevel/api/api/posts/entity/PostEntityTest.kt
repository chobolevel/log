package com.chobolevel.api.api.posts.entity

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostImage
import com.chobolevel.api.common.dummy.posts.DummyPostTag
import com.chobolevel.api.common.dummy.tags.DummyTag
import com.chobolevel.api.common.dummy.users.DummyUser
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// [엔티티 테스트 특징]
// - Spring 컨텍스트, DB, Mock 없이 순수 Kotlin 객체만 사용한다.
// - 엔티티 내부의 도메인 로직(연관관계 편의 메서드, 상태 변경 등)을 검증한다.
// - 가장 빠르고 단순한 테스트 계층이다.
@DisplayName("게시글 엔티티 단위 테스트")
class PostEntityTest {

    // [테스트 메서드 격리]
    // @BeforeEach로 공유 상태를 만들지 않고, 각 테스트 내에서 직접 객체를 생성한다.
    // → 실행 순서에 관계없이 항상 동일한 결과가 보장된다.

    @Test
    @DisplayName("게시글에 작성자를 매핑한다")
    fun `게시글_작성자_매핑`() {
        // given
        val post = DummyPost.toEntity()
        val user = DummyUser.toEntity()

        // when
        post.setBy(user)

        // then
        // Kotest shouldBe는 assertEquals와 동일하지만 코틀린 확장 함수이므로 가독성이 높다.
        post.user shouldBe user
    }

    @Test
    @DisplayName("이미 동일한 작성자가 설정되어 있으면 재설정해도 변하지 않는다")
    fun `게시글_동일_작성자_재설정_무시`() {
        // given
        val post = DummyPost.toEntity()
        val user = DummyUser.toEntity()
        post.setBy(user)

        // when: 같은 객체를 다시 설정 → Post.setBy 내부의 중복 방지 조건(this.user != user) 검증
        post.setBy(user)

        // then
        post.user shouldBe user
    }

    @Test
    @DisplayName("게시글에 태그를 추가한다")
    fun `게시글_태그_추가`() {
        // given
        val post = DummyPost.toEntity()
        val tag = DummyTag.toEntity()
        val postTag = DummyPostTag.toEntity()

        // when
        // PostTag.setBy(post) 내부에서 post.addPostTag(this)가 호출되므로
        // post.postTags에 자동으로 추가된다. (연관관계 편의 메서드 패턴)
        postTag.setBy(post)
        postTag.setBy(tag)

        // then
        post.postTags shouldHaveSize 1
        post.postTags.first() shouldBe postTag
        post.postTags.first().tag shouldBe tag
        post.postTags.first().post shouldBe post
    }

    @Test
    @DisplayName("동일한 태그는 중복 추가되지 않는다")
    fun `게시글_태그_중복_추가_방지`() {
        // given
        val post = DummyPost.toEntity()
        val postTag = DummyPostTag.toEntity()
        postTag.setBy(post) // 첫 번째 추가

        // when: 동일한 postTag를 직접 addPostTag로 재추가
        post.addPostTag(postTag)

        // then: Post.addPostTag 내부의 contains 체크로 중복이 방지되어야 한다.
        post.postTags shouldHaveSize 1
    }

    @Test
    @DisplayName("게시글에 이미지를 추가한다")
    fun `게시글_이미지_추가`() {
        // given
        val post = DummyPost.toEntity()
        val postImage = DummyPostImage.toEntity()

        // when
        post.addImage(postImage)

        // then
        post.images shouldHaveSize 1
        post.images.first() shouldBe postImage
    }

    @Test
    @DisplayName("게시글의 썸네일 이미지를 조회한다")
    fun `게시글_썸네일_이미지_조회`() {
        // given
        val post = DummyPost.toEntity()
        val postImage = DummyPostImage.toEntity()
        // PostImage.setBy(post) 내부에서 post.addImage(this)를 호출한다.
        postImage.setBy(post)

        // when
        val result = post.getThumbNailImage()

        // then
        // shouldNotBeNull()은 null이 아닌 것을 검증하고 non-null 타입을 반환한다.
        result.shouldNotBeNull()
        result shouldBe postImage
    }

    @Test
    @DisplayName("이미지가 없을 때 썸네일 이미지 조회 시 null을 반환한다")
    fun `게시글_썸네일_이미지_없음_null_반환`() {
        // given: 이미지가 추가되지 않은 게시글
        val post = DummyPost.toEntity()

        // when
        val result = post.getThumbNailImage()

        // then
        result.shouldBeNull()
    }

    @Test
    @DisplayName("게시글의 썸네일 이미지를 삭제 처리한다")
    fun `게시글_썸네일_이미지_삭제`() {
        // given
        val post = DummyPost.toEntity()
        val postImage = DummyPostImage.toEntity()
        postImage.setBy(post)

        // when
        post.deleteThumbNailImage()

        // then
        // deleteThumbNailImage는 images 컬렉션에서 제거하므로 비어있어야 한다.
        // JPA 환경에서는 @Where(clause="deleted=false")로 필터링되지만
        // 단위 테스트에서는 컬렉션에서 직접 제거된 것으로 검증한다.
        post.images.shouldBeEmpty()
        // 이미지의 deleted 플래그도 true가 되어야 한다.
        postImage.deleted shouldBe true
    }

    @Test
    @DisplayName("게시글을 소프트 딜리트(Soft Delete) 처리한다")
    fun `게시글_삭제`() {
        // given
        val post = DummyPost.toEntity()

        // when
        post.delete()

        // then
        // Soft Delete: DB에서 실제로 행을 삭제하지 않고 deleted 플래그만 변경한다.
        // 이 방식은 데이터 복구와 감사(audit) 추적이 가능하다는 장점이 있다.
        post.deleted shouldBe true
    }
}
