package com.chobolevel.api.post.service

import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyPostImage
import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.post.assembler.PostAssembler
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.dto.CreatePostRequest
import com.chobolevel.api.post.dto.PostResponse
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.api.post.updater.PostUpdater
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate

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
})
