package com.chobolevel.api.post.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.entity.QPost.post
import com.chobolevel.domain.post.repository.PostJpaRepository
import com.chobolevel.domain.post.repository.PostQuerydslRepository
import com.chobolevel.domain.post.vo.PostQueryFilter
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import com.querydsl.core.types.OrderSpecifier
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
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, PostQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("PostJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class PostJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var postJpaRepository: PostJpaRepository

    @Autowired
    private lateinit var postQuerydslRepository: PostQuerydslRepository

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

    @Test
    fun `삭제되지 않은 게시글을 id로 조회하면 게시글을 반환한다`() {
        // given
        val user: User = savedUser()
        val saved: Post = savedPost(user)
        entityManager.clear()

        // when
        val result: Post? = postJpaRepository.findByIdAndDeletedFalse(saved.id!!)

        // then
        assertThat(result).isNotNull
        assertThat(result!!.title).isEqualTo("테스트 게시글")
        assertThat(result.deleted).isFalse
    }

    @Test
    fun `삭제된 게시글을 id로 조회하면 null을 반환한다`() {
        // given
        val user: User = savedUser()
        val saved: Post = savedPost(user)
        saved.delete()
        entityManager.flush()
        entityManager.clear()

        // when
        val result: Post? = postJpaRepository.findByIdAndDeletedFalse(saved.id!!)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `QueryDSL로 제목 필터를 적용하면 해당 게시글만 조회한다`() {
        // given
        val user: User = savedUser()
        savedPost(user, title = "Kotlin 입문")
        savedPost(user, title = "Spring 심화")
        entityManager.clear()

        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null,
            title = "Kotlin",
            subTitle = null,
            userId = null
        )
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(post.createdAt.desc())

        // when
        val results: List<Post> = postQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().title).isEqualTo("Kotlin 입문")
    }

    @Test
    fun `QueryDSL로 집계하면 삭제되지 않은 게시글 수만 반환한다`() {
        // given
        val user: User = savedUser()
        val postA: Post = savedPost(user)
        savedPost(user, title = "다른 게시글")
        postA.delete()
        entityManager.flush()
        entityManager.clear()

        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null,
            title = null,
            subTitle = null,
            userId = null
        )

        // when
        val count: Long = postQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())

        // then
        assertThat(count).isEqualTo(1L)
    }
}
