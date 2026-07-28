package com.chobolevel.api.post.comment.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
import com.chobolevel.api.common.container.AbstractMySQLContainerTest
import com.chobolevel.domain.common.config.AuditConfiguration
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.post.comment.entity.PostComment
import com.chobolevel.domain.post.comment.entity.QPostComment.postComment
import com.chobolevel.domain.post.comment.repository.PostCommentJpaRepository
import com.chobolevel.domain.post.comment.repository.PostCommentQuerydslRepository
import com.chobolevel.domain.post.comment.vo.PostCommentQueryFilter
import com.chobolevel.domain.post.entity.Post
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
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, PostCommentQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("PostCommentJpaRepository Testcontainers(MySQL) 슬라이스 테스트")
class PostCommentJpaRepositoryContainerTest : AbstractMySQLContainerTest() {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var postCommentJpaRepository: PostCommentJpaRepository

    @Autowired
    private lateinit var postCommentQuerydslRepository: PostCommentQuerydslRepository

    private fun savedUser(email: String = "test@test.com"): User {
        return entityManager.persistAndFlush(
            User(
                email = email,
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "testUser",
                role = UserRoleType.ROLE_USER
            )
        )
    }

    private fun savedPost(user: User): Post {
        val p: Post = Post(title = "테스트 게시글", subTitle = "부제목", content = "내용")
        p.assignWriter(user)
        return entityManager.persistAndFlush(p)
    }

    private fun savedPostComment(post: Post, writer: User): PostComment {
        val comment: PostComment = PostComment(content = "테스트 댓글")
        comment.setBy(post)
        comment.setBy(writer)
        return entityManager.persistAndFlush(comment)
    }

    @Test
    fun `삭제되지 않은 댓글을 id로 조회하면 댓글을 반환한다`() {
        // given
        val user: User = savedUser()
        val post: Post = savedPost(user)
        val comment: PostComment = savedPostComment(post, user)
        entityManager.clear()

        // when
        val result: PostComment? = postCommentJpaRepository.findByIdAndDeletedFalse(comment.id!!)

        // then
        assertThat(result).isNotNull
        assertThat(result!!.content).isEqualTo("테스트 댓글")
        assertThat(result.deleted).isFalse
    }

    @Test
    fun `삭제된 댓글을 id로 조회하면 null을 반환한다`() {
        // given
        val user: User = savedUser()
        val post: Post = savedPost(user)
        val comment: PostComment = savedPostComment(post, user)
        comment.delete()
        entityManager.flush()
        entityManager.clear()

        // when
        val result: PostComment? = postCommentJpaRepository.findByIdAndDeletedFalse(comment.id!!)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `존재하지 않는 id로 조회하면 null을 반환한다`() {
        // when
        val result: PostComment? = postCommentJpaRepository.findByIdAndDeletedFalse(9999L)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `QueryDSL로 postId 필터를 적용하면 해당 게시글의 댓글만 조회한다`() {
        // given
        val user: User = savedUser()
        val postA: Post = savedPost(user)
        val postB: Post = savedPost(user)
        savedPostComment(postA, user)
        savedPostComment(postB, user)
        entityManager.clear()

        val queryFilter: PostCommentQueryFilter = PostCommentQueryFilter(postId = postA.id, writerId = null)
        val paging: Paging = Paging(page = 1, size = 10)
        val orderSpecifiers: Array<OrderSpecifier<*>> = arrayOf(postComment.createdAt.asc())

        // when
        val results: List<PostComment> = postCommentQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderSpecifiers
        )

        // then
        assertThat(results).hasSize(1)
        assertThat(results.first().post!!.id).isEqualTo(postA.id)
    }

    @Test
    fun `QueryDSL로 필터 없이 집계하면 삭제되지 않은 댓글 수만 반환한다`() {
        // given
        val user: User = savedUser()
        val post: Post = savedPost(user)
        val commentA: PostComment = savedPostComment(post, user)
        savedPostComment(post, user)
        commentA.delete()
        entityManager.flush()
        entityManager.clear()

        val queryFilter: PostCommentQueryFilter = PostCommentQueryFilter(postId = null, writerId = null)

        // when
        val count: Long = postCommentQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())

        // then
        assertThat(count).isEqualTo(1L)
    }
}
