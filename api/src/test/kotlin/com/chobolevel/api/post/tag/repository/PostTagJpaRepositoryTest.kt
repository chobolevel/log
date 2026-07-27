package com.chobolevel.api.post.tag.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.tag.entity.PostTag
import com.chobolevel.domain.post.tag.repository.PostTagJpaRepository
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class)
@ActiveProfiles("test")
@DisplayName("PostTagJpaRepository 슬라이스 테스트")
class PostTagJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var postTagJpaRepository: PostTagJpaRepository

    private fun savedUser(): User {
        return entityManager.persistAndFlush(
            User(
                email = "test@test.com",
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "testUser",
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedPost(user: User, title: String = "테스트 게시글"): Post {
        val post: Post = Post(title = title, subTitle = "부제목", content = "내용")
        post.assignWriter(user)
        return entityManager.persistAndFlush(post)
    }

    private fun savedTag(): Tag {
        return entityManager.persistAndFlush(Tag(name = "Kotlin", order = 1))
    }

    // Post.postTags 컬렉션 cascade를 우회하기 위해 PostTag를 직접 persist한다.
    // cascade를 통해 삽입하면 flush 시 컬렉션이 재삽입을 유발해 deleteByPostId 테스트가 실패한다.
    private fun savedPostTag(post: Post, tag: Tag): PostTag {
        val postTag: PostTag = PostTag()
        postTag.assignPost(post)
        postTag.assignTag(tag)
        return entityManager.persistAndFlush(postTag)
    }

    @Test
    fun `게시글 id로 모든 PostTag를 삭제하면 해당 게시글의 태그가 전부 제거된다`() {
        // given
        val user: User = savedUser()
        val post: Post = savedPost(user)
        val tag: Tag = savedTag()
        savedPostTag(post, tag)
        entityManager.clear()

        // when
        postTagJpaRepository.deleteByPostId(post.id!!)
        // entityManager.clear()를 호출하지 않는다.
        // clear()는 REMOVED 상태의 엔티티도 함께 날려 DELETE SQL이 실행되지 않는다.
        // count() 호출 시 FlushMode.AUTO가 자동으로 flush를 수행해 DELETE가 먼저 실행된다.

        // then
        assertThat(postTagJpaRepository.count()).isEqualTo(0L)
    }

    @Test
    fun `다른 게시글의 PostTag는 삭제되지 않는다`() {
        // given
        val user: User = savedUser()
        val postA: Post = savedPost(user, title = "게시글A")
        val postB: Post = savedPost(user, title = "게시글B")
        val tag: Tag = savedTag()
        savedPostTag(postA, tag)
        savedPostTag(postB, tag)
        entityManager.clear()

        // when
        postTagJpaRepository.deleteByPostId(postA.id!!)
        // FlushMode.AUTO: findAll() 호출 전 자동 flush되어 DELETE가 먼저 실행된다.

        // then - postB의 PostTag는 남아 있어야 한다
        val remaining: List<PostTag> = postTagJpaRepository.findAll()
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().post!!.id).isEqualTo(postB.id)
    }
}
