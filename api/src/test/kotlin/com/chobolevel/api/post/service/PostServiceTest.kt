package com.chobolevel.api.post.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyPostImage
import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.post.assembler.PostAssembler
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.dto.CreatePostRequest
import com.chobolevel.api.post.dto.PostPagingRequest
import com.chobolevel.api.post.dto.PostResponse
import com.chobolevel.api.post.dto.SearchPostRequest
import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.api.post.updater.PostUpdater
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.post.vo.PostQueryFilter
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

class PostServiceTest : BehaviorSpec({

    val postRepository: PostRepository = mockk()
    val userRepository: UserRepository = mockk()
    val tagRepository: TagRepository = mockk()
    val postConverter: PostConverter = mockk()
    val postImageConverter: PostImageConverter = mockk()
    val postAssembler: PostAssembler = PostAssembler()
    val postUpdater: PostUpdater = mockk()
    val postUpdaters: List<PostUpdater> = listOf(postUpdater)
    val redisTemplate: RedisTemplate<String, PostResponse> = mockk()
    val postService: PostService = PostService(
        postRepository = postRepository,
        userRepository = userRepository,
        tagRepository = tagRepository,
        postConverter = postConverter,
        postAssembler = postAssembler,
        postImageConverter = postImageConverter,
        postUpdaters = postUpdaters,
        redisTemplate = redisTemplate,
    )

    beforeEach {
        clearAllMocks()
    }

    given("게시글 등록할 때") {
        `when`("썸네일 이미지가 있는 유효한 요청이 들어오면") {
            then("저장된 게시글의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val request: CreatePostRequest = DummyPost.toCreateRequest()

                val user: User = DummyUser.toEntity()
                val post: Post = DummyPost.toEntity()
                val postImage: PostImage = DummyPostImage.toEntity()
                val tags: List<Tag> = listOf(DummyTag.toEntity())
                // save()에 전달될 Post 인수를 담을 그릇
                val savedPostSlot: CapturingSlot<Post> = slot<Post>()

                every { userRepository.findById(id = userId) } returns user
                every { postConverter.convert(request = request) } returns post
                every { postImageConverter.convert(request = request.thumbnailImage!!) } returns postImage
                every { tagRepository.findByIds(request.tagIds) } returns tags
                // capture()로 실제 전달된 인수를 슬롯에 기록
                every { postRepository.save(capture(savedPostSlot)) } returns post

                // when
                val result: Long = postService.createPost(userId = userId, request = request)

                // then
                result shouldBe DummyPost.ID
                verify { postImageConverter.convert(request.thumbnailImage!!) }
                verify { postRepository.save(any()) }
                // savedPostSlot.captured: 실제로 save()에 넘어온 Post 인스턴스
                savedPostSlot.captured.user shouldBe user
                savedPostSlot.captured.postImages.size shouldBe 1
                savedPostSlot.captured.getThumbnailImage() shouldBe postImage
            }
        }

        `when`("썸네일 이미지가 없는 유효한 요청이 들어오면") {
            then("저장된 게시글의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val request: CreatePostRequest = DummyPost.toCreateRequestWithoutThumbnail()

                val user: User = DummyUser.toEntity()
                val post: Post = DummyPost.toEntity()
                val tags: List<Tag> = listOf(DummyTag.toEntity())
                val savedPostSlot: CapturingSlot<Post> = slot<Post>()

                every { userRepository.findById(id = userId) } returns user
                every { postConverter.convert(request = request) } returns post
                every { tagRepository.findByIds(request.tagIds) } returns tags
                every { postRepository.save(capture(savedPostSlot)) } returns post

                // when
                val result: Long = postService.createPost(userId = userId, request = request)

                // then
                result shouldBe DummyPost.ID
                verify(exactly = 0) { postImageConverter.convert(any<CreatePostImageRequest>()) }
                verify { postRepository.save(any()) }
                savedPostSlot.captured.user shouldBe user
                savedPostSlot.captured.postImages.size shouldBe 0 // 이미지 미첨부 확인
            }
        }
    }

    given("게시글 목록을 조회할 때") {
        `when`("게시글이 존재하면") {
            then("페이징 정보와 게시글 목록을 반환한다") {
                // given
                val filter: SearchPostRequest = DummyPost.toSearchRequest()
                val pageRequest: PostPagingRequest = PostPagingRequest()

                val queryFilter: PostQueryFilter = PostQueryFilter(
                    tagId = null,
                    title = null,
                    subTitle = null,
                    userId = null
                )
                val posts: List<Post> = listOf(DummyPost.toEntity())
                val postResponses: List<PostResponse> = listOf(DummyPost.toResponse())
                val totalCount: Long = 1L

                every { postConverter.convert(request = filter) } returns queryFilter
                every { postRepository.searchPosts(queryFilter = queryFilter, paging = any(), orderTypes = any()) } returns posts
                every { postRepository.searchPostsCount(queryFilter) } returns totalCount
                every { postConverter.convert(entities = posts) } returns postResponses

                // when
                val result: PagingResponse = postService.searchPosts(filter = filter, pageRequest = pageRequest)

                // then
                result.page shouldBe pageRequest.page
                result.size shouldBe pageRequest.size
                result.totalCount shouldBe totalCount
                result.data shouldBe postResponses
                verify { postRepository.searchPosts(queryFilter = queryFilter, paging = any(), orderTypes = any()) }
                verify { postRepository.searchPostsCount(queryFilter) }
            }
        }

        `when`("검색 결과가 없으면") {
            then("빈 목록과 totalCount 0을 반환한다") {
                // given
                val filter: SearchPostRequest = DummyPost.toSearchRequest()
                val pageRequest: PostPagingRequest = PostPagingRequest()

                val queryFilter: PostQueryFilter = PostQueryFilter(
                    tagId = null,
                    title = null,
                    subTitle = null,
                    userId = null
                )
                val emptyPosts: List<Post> = emptyList()
                val emptyResponses: List<PostResponse> = emptyList()
                val totalCount: Long = 0L

                every { postConverter.convert(request = filter) } returns queryFilter
                every { postRepository.searchPosts(queryFilter = queryFilter, paging = any(), orderTypes = any()) } returns emptyPosts
                every { postRepository.searchPostsCount(queryFilter) } returns totalCount
                every { postConverter.convert(entities = emptyPosts) } returns emptyResponses

                // when
                val result: PagingResponse = postService.searchPosts(filter = filter, pageRequest = pageRequest)

                // then
                result.totalCount shouldBe 0L
                result.data shouldBe emptyList<PostResponse>()
                verify { postRepository.searchPosts(queryFilter = queryFilter, paging = any(), orderTypes = any()) }
                verify { postRepository.searchPostsCount(queryFilter) }
            }
        }
    }

    given("게시글 단건 조회할 때") {
        `when`("캐시가 없으면") {
            then("DB에서 조회 후 캐시에 저장하고 응답을 반환한다") {
                // given
                val postId: Long = DummyPost.ID
                val cachingKey: String = "post:$postId"
                val post: Post = DummyPost.toEntity().also { it.assignWriter(DummyUser.toEntity()) }
                val postResponse: PostResponse = DummyPost.toResponse()
                val valueOperations: ValueOperations<String, PostResponse> = mockk()

                every { redisTemplate.opsForValue() } returns valueOperations
                every { valueOperations.get(cachingKey) } returns null
                every { postRepository.findById(postId) } returns post
                every { postConverter.convert(entity = post) } returns postResponse
                justRun { valueOperations.set(any(), any(), any(), any()) }

                // when
                val result: PostResponse = postService.fetchPost(postId)

                // then
                result shouldBe postResponse
                verify { postRepository.findById(postId) }
                verify { valueOperations.set(cachingKey, postResponse, any(), any()) }
            }
        }

        `when`("캐시가 있으면") {
            then("DB 조회 없이 캐시에서 응답을 반환한다") {
                // given
                val postId: Long = DummyPost.ID
                val cachingKey: String = "post:$postId"
                val cachedPostResponse: PostResponse = DummyPost.toResponse()
                val valueOperations: ValueOperations<String, PostResponse> = mockk()

                every { redisTemplate.opsForValue() } returns valueOperations
                every { valueOperations.get(cachingKey) } returns cachedPostResponse

                // when
                val result: PostResponse = postService.fetchPost(postId)

                // then
                result shouldBe cachedPostResponse
                verify(exactly = 0) { postRepository.findById(any()) }
            }
        }
    }

    given("게시글 수정할 때") {
        `when`("작성자가 수정 요청하면") {
            then("수정된 게시글의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val postId: Long = DummyPost.ID
                val request: UpdatePostRequest = DummyPost.toUpdateRequest()
                val post: Post = DummyPost.toEntity().also { it.assignWriter(DummyUser.toEntity()) }

                every { postRepository.findById(postId) } returns post
                every { postUpdater.order() } returns 0
                every { postUpdater.markAsUpdate(request, post) } returns post
                every { redisTemplate.delete(any<String>()) } returns true

                // when
                val result: Long = postService.updatePost(userId = userId, postId = postId, request = request)

                // then
                result shouldBe DummyPost.ID
                verify { postUpdater.markAsUpdate(request, post) }
                verify { redisTemplate.delete("post:$postId") }
            }
        }

        `when`("작성자가 아닌 사람이 수정 요청하면") {
            then("POST_ONLY_ACCESS_WRITER 예외가 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1
                val postId: Long = DummyPost.ID
                val request: UpdatePostRequest = DummyPost.toUpdateRequest()
                val post: Post = DummyPost.toEntity().also { it.assignWriter(DummyUser.toEntity()) }

                every { postRepository.findById(postId) } returns post

                // when / then
                val exception: ApiException = shouldThrow<ApiException> {
                    postService.updatePost(userId = otherUserId, postId = postId, request = request)
                }
                exception.errorCode shouldBe ErrorCode.POST_ONLY_ACCESS_WRITER
                verify(exactly = 0) { postUpdater.markAsUpdate(any(), any()) }
            }
        }
    }

    given("게시글 삭제할 때") {
        `when`("작성자가 삭제 요청하면") {
            then("true를 반환하고 게시글이 삭제 처리된다") {
                // given
                val userId: Long = DummyUser.ID
                val postId: Long = DummyPost.ID
                val post: Post = DummyPost.toEntity().also { it.assignWriter(DummyUser.toEntity()) }

                every { postRepository.findById(postId) } returns post
                every { redisTemplate.delete(any<String>()) } returns true

                // when
                val result: Boolean = postService.deletePost(userId = userId, postId = postId)

                // then
                result shouldBe true
                post.deleted shouldBe true
                verify { redisTemplate.delete("post:$postId") }
            }
        }

        `when`("작성자가 아닌 사람이 삭제 요청하면") {
            then("POST_ONLY_ACCESS_WRITER 예외가 발생한다") {
                // given
                val otherUserId: Long = DummyUser.ID + 1
                val postId: Long = DummyPost.ID
                val post: Post = DummyPost.toEntity().also { it.assignWriter(DummyUser.toEntity()) }

                every { postRepository.findById(postId) } returns post

                // when / then
                val exception: ApiException = shouldThrow<ApiException> {
                    postService.deletePost(userId = otherUserId, postId = postId)
                }
                exception.errorCode shouldBe ErrorCode.POST_ONLY_ACCESS_WRITER
                post.deleted shouldBe false
            }
        }
    }
})
