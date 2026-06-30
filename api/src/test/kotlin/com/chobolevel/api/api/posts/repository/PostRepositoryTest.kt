package com.chobolevel.api.api.posts.repository

import com.chobolevel.domain.DomainConfigurationLoader
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.post.PostFinder
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.entity.PostOrderType
import com.chobolevel.domain.post.repository.PostCustomRepository
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.post.vo.PostQueryFilter
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.entity.UserRoleType
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

// [레포지토리 통합 테스트 특징]
// - @DataJpaTest: JPA 관련 컴포넌트(EntityManager, DataSource, JpaRepository)만 로드한다.
//   Service, Controller 등은 로드하지 않으므로 @SpringBootTest보다 훨씬 빠르게 실행된다.
// - 검증 범위: QueryDSL 동적 쿼리, 정렬, 페이지네이션, 필터, 소프트 딜리트 동작
//
// - @Import(DomainConfigurationLoader::class):
//   domain 모듈의 @EntityScan, @EnableJpaRepositories를 활성화한다.
//   이 설정 없이는 com.chobolevel.domain 하위의 Entity와 Repository를 인식하지 못한다.
//
// - @Import(PostCustomRepository::class, PostFinder::class):
//   JpaRepository를 상속하지 않는 QueryDSL 커스텀 클래스는 @DataJpaTest의
//   자동 스캔 범위 밖일 수 있으므로 명시적으로 등록한다.
//
// [H2 인메모리 DB 한계]
// - @DataJpaTest는 기본적으로 DataSource를 H2로 교체한다(Replace.ANY).
// - @SQLDelete 등 MySQL 전용 SQL 구문이 있다면 H2에서 실패할 수 있다.
//   MySQL 방언이 필요한 경우 AbstractIntegrationTest(Testcontainers)를 사용한다.
@DisplayName("게시글 레포지토리 통합 테스트")
@DataJpaTest
@Import(
    DomainConfigurationLoader::class,
    PostCustomRepository::class,
    PostFinder::class
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PostRepositoryTest {

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var postFinder: PostFinder

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var savedUser: User

    // 테스트 간 상태 오염 방지: @BeforeEach에서 DB를 초기화하고 픽스처를 재생성한다.
    // @DataJpaTest는 기본적으로 @Transactional을 적용하여 각 테스트 종료 후 롤백하지만,
    // 명시적으로 deleteAll()을 호출하면 의도가 더 명확하게 드러난다.
    @BeforeEach
    fun setUp() {
        postRepository.deleteAll()
        userRepository.deleteAll()

        savedUser = userRepository.save(
            User(
                email = "test@test.com",
                password = "password",
                socialId = null,
                loginType = UserLoginType.GENERAL,
                nickname = "테스터",
                role = UserRoleType.ROLE_USER
            )
        )

        postRepository.save(
            Post(title = "코틀린 인 액션", subTitle = "코틀린 학습", content = "코틀린 내용")
                .also { it.setBy(savedUser) }
        )
        postRepository.save(
            Post(title = "자바의 정석", subTitle = "자바 학습", content = "자바 내용")
                .also { it.setBy(savedUser) }
        )
    }

    // ==================== search ====================

    @Test
    @DisplayName("필터 없이 조회하면 전체 게시글 목록을 반환한다")
    fun `전체_게시글_목록_조회`() {
        // given
        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null, title = null, subTitle = null, userId = null
        )
        val pagination: Pagination = Pagination(offset = 0, limit = 10)

        // when
        val result: List<Post> = postFinder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = listOf(PostOrderType.CREATED_AT_DESC)
        )

        // then
        result.size shouldBe 2
    }

    @Test
    @DisplayName("제목으로 필터링하면 일치하는 게시글만 반환한다")
    fun `제목_필터_게시글_조회`() {
        // given
        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null, title = "코틀린", subTitle = null, userId = null
        )
        val pagination: Pagination = Pagination(offset = 0, limit = 10)

        // when
        val result: List<Post> = postFinder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = listOf(PostOrderType.CREATED_AT_DESC)
        )

        // then
        result.size shouldBe 1
        result[0].title shouldBe "코틀린 인 액션"
    }

    @Test
    @DisplayName("작성자 ID로 필터링하면 해당 작성자의 게시글만 반환한다")
    fun `작성자_ID_필터_게시글_조회`() {
        // given
        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null, title = null, subTitle = null, userId = savedUser.id
        )
        val pagination: Pagination = Pagination(offset = 0, limit = 10)

        // when
        val result: List<Post> = postFinder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = listOf(PostOrderType.CREATED_AT_ASC)
        )

        // then: 작성자의 게시글 2건이 CREATED_AT_ASC 정렬로 반환된다
        result.size shouldBe 2
        result[0].title shouldBe "코틀린 인 액션"
        result[1].title shouldBe "자바의 정석"
    }

    @Test
    @DisplayName("페이지네이션이 정확하게 적용된다")
    fun `페이지네이션_적용`() {
        // given: 2건 중 limit=1 → 1건만 반환해야 한다
        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null, title = null, subTitle = null, userId = null
        )
        val pagination: Pagination = Pagination(offset = 0, limit = 1)

        // when
        val result: List<Post> = postFinder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = listOf(PostOrderType.CREATED_AT_DESC)
        )

        // then
        result.size shouldBe 1
    }

    // ==================== searchCount ====================

    @Test
    @DisplayName("필터 없이 카운트하면 전체 게시글 수를 반환한다")
    fun `전체_게시글_카운트`() {
        // given
        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null, title = null, subTitle = null, userId = null
        )

        // when
        val totalCount: Long = postFinder.searchCount(queryFilter)

        // then
        totalCount shouldBe 2L
    }

    @Test
    @DisplayName("소프트 딜리트된 게시글은 목록과 카운트에서 제외된다")
    fun `소프트_딜리트_게시글_조회_제외`() {
        // given: 첫 번째 게시글 소프트 딜리트
        val postToDelete: Post = postRepository.findAll().first()
        postToDelete.delete()
        postRepository.save(postToDelete)

        // [주의]: @DataJpaTest의 1차 캐시(영속성 컨텍스트)에 이전 상태가 남아 있을 수 있다.
        // flush + clear로 DB에 반영하고 캐시를 비워야 실제 쿼리 결과를 검증할 수 있다.
        postRepository.flush()

        val queryFilter: PostQueryFilter = PostQueryFilter(
            tagId = null, title = null, subTitle = null, userId = null
        )
        val pagination: Pagination = Pagination(offset = 0, limit = 10)

        // when
        val result: List<Post> = postFinder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = listOf(PostOrderType.CREATED_AT_DESC)
        )
        val totalCount: Long = postFinder.searchCount(queryFilter)

        // then: 소프트 딜리트 적용 → @Where(clause = "deleted = false")로 필터링
        result.size shouldBe 1
        totalCount shouldBe 1L
    }
}
