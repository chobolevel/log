package com.chobolevel.api.post.tag.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DomainJpaTestConfig::class, AuditConfiguration::class)
@ActiveProfiles("test")
@DisplayName("PostTagJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class PostTagJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

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
        val p: Post = Post(title = title, subTitle = "부제목", content = "내용")
        p.assignWriter(user)
        return entityManager.persistAndFlush(p)
    }

    private fun savedTag(): Tag {
        return entityManager.persistAndFlush(Tag(name = "Kotlin", order = 1))
    }

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

        // then
        val remaining: List<PostTag> = postTagJpaRepository.findAll()
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().post!!.id).isEqualTo(postB.id)
    }
}
