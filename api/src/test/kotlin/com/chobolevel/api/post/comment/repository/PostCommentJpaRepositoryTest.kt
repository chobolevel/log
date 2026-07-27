package com.chobolevel.api.post.comment.repository

import com.chobolevel.api.common.config.DomainJpaTestConfig
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// @DataJpaTest는 @SpringBootApplication 패키지(com.chobolevel.api) 기준으로 리포지토리를 스캔한다.
// 도메인 리포지토리는 com.chobolevel.domain에 있으므로, DomainJpaTestConfig를 통해
// @EnableJpaRepositories와 @EntityScan을 도메인 패키지 기준으로 적용해야 한다.
// DomainConfigurationLoader 대신 별도 TestConfiguration을 사용하는 이유:
// DomainConfigurationLoader의 @ComponentScan이 EmailUtils 등 비 JPA 빈도 로드해
// JavaMailSender 의존성 문제를 일으키기 때문이다.
@DataJpaTest
@Import(DomainJpaTestConfig::class, AuditConfiguration::class, PostCommentQuerydslRepository::class)
@ActiveProfiles("test")
@DisplayName("PostCommentJpaRepository 슬라이스 테스트")
class PostCommentJpaRepositoryTest {

    // TestEntityManager: EntityManager 래퍼로, 테스트 데이터 셋업에 사용한다.
    // persistAndFlush()는 저장 즉시 DB에 반영하고 flush한다.
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
        val post: Post = Post(title = "테스트 게시글", subTitle = "부제목", content = "내용")
        post.assignWriter(user)
        return entityManager.persistAndFlush(post)
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
        // clear로 1차 캐시를 비워 DB에서 실제로 조회되는지 검증한다.
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
        // given - 댓글 2개 생성 후 1개 삭제
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
