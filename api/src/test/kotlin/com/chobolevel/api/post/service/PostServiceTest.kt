package com.chobolevel.api.post.service

import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.dto.CreatePostRequest
import com.chobolevel.api.post.dto.PostResponse
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.post.image.dto.CreatePostImageRequest
import com.chobolevel.api.post.updater.PostUpdater
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate

class PostServiceTest : BehaviorSpec({

    val postRepository: PostRepository = mockk()
    val userRepository: UserRepository = mockk()
    val tagRepository: TagRepository = mockk()
    val postConverter: PostConverter = mockk()
    val postImageConverter: PostImageConverter = mockk()
    val postUpdater: PostUpdater = mockk()
    val postUpdaters: List<PostUpdater> = listOf(postUpdater)
    val redisTemplate: RedisTemplate<String, PostResponse> = mockk()
    val postService: PostService = PostService(
        repository = postRepository,
        userRepository = userRepository,
        tagRepository = tagRepository,
        converter = postConverter,
        postImageConverter = postImageConverter,
        updaters = postUpdaters,
        redisTemplate = redisTemplate,
    )

    beforeEach {
        clearAllMocks()
    }

    given("게시글 등록할 때") {
        `when`("유효한 요청이 들어오면") {
            then("저장된 게시글의 id를 반환한다") {
                // given
                val userId: Long = DummyUser.ID
                val request: CreatePostRequest = CreatePostRequest(
                    tagIds = listOf(DummyTag.ID),
                    title = DummyPost.TITLE,
                    subTitle = DummyPost.SUB_TITLE,
                    content = DummyPost.CONTENT,
                    thumbNailImage = null
                )
                val user: User = DummyUser.toEntity()
                val post: Post = DummyPost.toEntity()
                val tags: List<Tag> = listOf(DummyTag.toEntity())
                every { userRepository.findById(userId) } returns user
                every { postConverter.convert(request) } returns post
                every { tagRepository.findByIds(request.tagIds) } returns tags
                every { postRepository.save(post) } returns post
                // 실해되는 조건으로 변경 필요
                verify(exactly = 0) { postImageConverter.convert(any<CreatePostImageRequest>()) }

                // when
                val result: Long = postService.createPost(
                    userId = userId,
                    request = request
                )

                // then
                result shouldBe DummyPost.ID
            }
        }
    }
})
