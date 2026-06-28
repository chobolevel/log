package com.chobolevel.api.api.posts.service

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.tags.DummyTag
import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.converter.PostImageConverter
import com.chobolevel.api.post.dto.PostResponseDto
import com.chobolevel.api.post.service.PostService
import com.chobolevel.api.post.updater.PostUpdatable
import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.post.Post
import com.chobolevel.domain.post.PostFinder
import com.chobolevel.domain.post.PostOrderType
import com.chobolevel.domain.post.PostQueryFilter
import com.chobolevel.domain.post.PostRepository
import com.chobolevel.domain.tag.TagFinder
import com.chobolevel.domain.user.UserFinder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

// [서비스 테스트 특징]
// - 서비스 계층은 비즈니스 로직의 핵심이다. 가장 꼼꼼히 테스트해야 한다.
// - Repository, Finder 등 외부 의존성은 모두 Mock으로 대체하여 '서비스 로직만' 검증한다.
// - 성공 케이스와 실패 케이스를 반드시 함께 작성한다.
@DisplayName("게시글 서비스 단위 테스트")
@ExtendWith(MockKExtension::class)
class PostServiceTest {

    // 의존성을 하나씩 명시적으로 선언하면 어떤 의존성이 있는지 파악하기 쉽다.
    private val repository: PostRepository = mockk()
    private val finder: PostFinder = mockk()
    private val userFinder: UserFinder = mockk()
    private val tagFinder: TagFinder = mockk()
    private val converter: PostConverter = mockk()
    private val postImageConverter: PostImageConverter = mockk()
    private val postUpdatable: PostUpdatable = mockk()

    // RedisTemplate은 제네릭 타입을 가지므로 타입 파라미터를 명시한다.
    private val redisTemplate: RedisTemplate<String, PostResponseDto> = mockk()

    // ValueOperations: Redis의 일반 값(String, Object) 조작 인터페이스
    private val valueOperations: ValueOperations<String, PostResponseDto> = mockk()

    // 테스트 대상(SUT)
    private lateinit var postService: PostService

    // [List<PostUpdatable> 주입 문제]
    // @InjectMocks(Mockito) 또는 @InjectMockKs(MockK)는 컬렉션 타입 의존성을 자동 주입하지 못한다.
    // @BeforeEach에서 직접 생성자에 주입하면 이 문제를 해결하고, 의존성이 더 명확하게 보인다.
    @BeforeEach
    fun setUp() {
        postService = PostService(
            repository = repository,
            finder = finder,
            userFinder = userFinder,
            tagFinder = tagFinder,
            converter = converter,
            postImageConverter = postImageConverter,
            updaters = listOf(postUpdatable),
            redisTemplate = redisTemplate
        )
    }

    // ==================== createPost ====================

    @Test
    @DisplayName("게시글을 등록하면 생성된 게시글의 ID를 반환한다")
    fun `게시글_등록_성공`() {
        // given
        val userId = DummyUser.id
        val request = DummyPost.toCreateRequestDto()
        val user = DummyUser.toEntity()
        val post = DummyPost.toEntity()
        val tags = listOf(DummyTag.toEntity())

        // Mock 설정: "이 메서드가 호출되면 이 값을 반환해라"
        every { userFinder.findById(userId) } returns user
        every { converter.convert(request) } returns post
        every { tagFinder.findByIds(request.tagIds) } returns tags
        // repository.save()는 저장된 엔티티(id 포함)를 반환해야 한다.
        every { repository.save(post) } returns post

        // when
        val result = postService.createPost(userId, request)

        // then
        result shouldBe DummyPost.ID

        // [verify]: 특정 메서드가 실제로 호출되었는지 검증한다.
        // 단순히 반환값만 확인하는 것이 아니라 '행위(behavior)'를 검증하는 것이다.
        verify { repository.save(post) }
    }

    // ==================== searchPosts ====================

    @Test
    @DisplayName("게시글 목록을 조회하면 페이징된 결과를 반환한다")
    fun `게시글_목록_조회_성공`() {
        // given
        val queryFilter = PostQueryFilter(tagId = null, title = null, subTitle = null, userId = null)
        val pagination = Pagination(offset = 0, limit = 10)
        val orderTypes = listOf(PostOrderType.CREATED_AT_DESC)

        val posts: List<Post> = listOf(DummyPost.toEntity())
        val postResponses: List<PostResponseDto> = listOf(DummyPost.toResponseDto())
        val totalCount = 1L

        every { finder.search(queryFilter, pagination, orderTypes) } returns posts
        every { finder.searchCount(queryFilter) } returns totalCount
        every { converter.convert(posts) } returns postResponses

        // when
        val result = postService.searchPosts(queryFilter, pagination, orderTypes)

        // then
        result.skipCount shouldBe pagination.offset
        result.limitCount shouldBe pagination.limit
        result.totalCount shouldBe totalCount
        result.data.size shouldBe postResponses.size
    }

    // ==================== fetchPost ====================

    @Test
    @DisplayName("캐시 미스(Cache Miss) 시 DB에서 조회하여 캐시에 저장하고 반환한다")
    fun `게시글_단건_조회_캐시_미스`() {
        // given
        val postId = DummyPost.ID
        val cachingKey = "post:$postId"
        val post = DummyPost.toEntity()
        val postResponse = DummyPost.toResponseDto()

        // [Cache Miss 시나리오]
        // 1. Redis에서 조회했을 때 null 반환 (캐시에 없음)
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(cachingKey) } returns null // cache miss

        // 2. DB에서 조회
        every { finder.findById(postId) } returns post
        every { converter.convert(post) } returns postResponse

        // 3. Redis에 저장 (just Runs: Unit 반환 메서드를 Mock할 때 사용)
        every { valueOperations.set(cachingKey, postResponse, 10, TimeUnit.MINUTES) } just Runs

        // when
        val result = postService.fetchPost(postId)

        // then
        result shouldBe postResponse

        // 캐시 저장이 실제로 호출되었는지 검증 (Cache-Aside 패턴 검증)
        verify { valueOperations.set(cachingKey, postResponse, 10, TimeUnit.MINUTES) }
    }

    @Test
    @DisplayName("캐시 히트(Cache Hit) 시 Redis에서 바로 반환한다")
    fun `게시글_단건_조회_캐시_히트`() {
        // given
        val postId = DummyPost.ID
        val cachingKey = "post:$postId"
        val cachedResponse = DummyPost.toResponseDto()

        // [Cache Hit 시나리오]: Redis에서 이미 캐시된 데이터 반환
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.get(cachingKey) } returns cachedResponse // cache hit

        // when
        val result = postService.fetchPost(postId)

        // then
        result shouldBe cachedResponse

        // DB 조회(finder.findById)가 호출되지 않았는지 검증
        // was Not Called: 이 메서드가 한 번도 호출되지 않아야 한다.
        verify(exactly = 0) { finder.findById(any()) }
    }

    // ==================== updatePost ====================

    @Test
    @DisplayName("작성자가 게시글을 수정하면 게시글 ID를 반환한다")
    fun `게시글_수정_성공`() {
        // given
        val userId = DummyUser.id
        val postId = DummyPost.ID
        val request = DummyPost.toUpdateRequestDto()

        // post.user.id == userId 여야 수정 권한이 있다.
        val post = DummyPost.toEntity().also { it.setBy(DummyUser.toEntity()) }

        every { finder.findById(postId) } returns post
        // sortedBy { it.order() }가 호출되므로 order() 메서드도 Stubbing해야 한다.
        every { postUpdatable.order() } returns 0
        every { postUpdatable.markAsUpdate(request, post) } returns post
        // redisTemplate.delete()는 Boolean?을 반환한다.
        every { redisTemplate.delete(any<String>()) } returns true

        // when
        val result = postService.updatePost(userId, postId, request)

        // then
        result shouldBe postId
        verify { redisTemplate.delete("post:$postId") }
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 수정하려 하면 예외가 발생한다")
    fun `게시글_수정_권한_없음_예외`() {
        // given
        val othersUserId = 999L // 작성자(id=1)가 아닌 다른 사용자
        val postId = DummyPost.ID

        // post의 작성자는 DummyUser (id=1)이고, 수정 요청자는 id=999다.
        val post = DummyPost.toEntity().also { it.setBy(DummyUser.toEntity()) }

        every { finder.findById(postId) } returns post

        // when & then
        shouldThrow<ApiException> {
            postService.updatePost(othersUserId, postId, DummyPost.toUpdateRequestDto())
        }
    }

    // ==================== deletePost ====================

    @Test
    @DisplayName("작성자가 게시글을 삭제하면 true를 반환한다")
    fun `게시글_삭제_성공`() {
        // given
        val userId = DummyUser.id
        val postId = DummyPost.ID
        val post = DummyPost.toEntity().also { it.setBy(DummyUser.toEntity()) }

        every { finder.findById(postId) } returns post
        every { redisTemplate.delete(any<String>()) } returns true

        // when
        val result = postService.deletePost(userId, postId)

        // then
        result shouldBe true

        // Soft Delete가 실제로 적용되었는지 검증
        post.deleted shouldBe true

        // 게시글 삭제 시 캐시도 무효화(invalidation)되어야 한다.
        verify { redisTemplate.delete("post:$postId") }
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 삭제하려 하면 예외가 발생한다")
    fun `게시글_삭제_권한_없음_예외`() {
        // given
        val othersUserId = 999L
        val postId = DummyPost.ID
        val post = DummyPost.toEntity().also { it.setBy(DummyUser.toEntity()) }

        every { finder.findById(postId) } returns post

        // when & then
        // shouldThrow는 예외 발생을 검증하고 예외 객체를 반환하므로 추가 검증도 가능하다.
        val ex = shouldThrow<ApiException> {
            postService.deletePost(othersUserId, postId)
        }
        ex.message shouldBe "작성자만 접근할 수 있습니다."
    }
}
