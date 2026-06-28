package com.chobolevel.api.api.posts.converter

import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dummy.posts.DummyPostImage
import com.chobolevel.api.common.dummy.posts.DummyPostTag
import com.chobolevel.api.common.dummy.tags.DummyTag
import com.chobolevel.api.common.dummy.users.DummyUser
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.converter.PostImageConverter
import com.chobolevel.api.tag.converter.TagConverter
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.domain.user.User
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// [Converter 테스트 특징]
// - Converter는 '객체 변환' 책임만 가지므로 DB, 네트워크 없이 테스트 가능하다.
// - PostConverter가 의존하는 하위 Converter들은 Mock으로 대체하여 PostConverter 로직만 검증한다.
// - 이로써 하위 Converter에 버그가 있어도 PostConverterTest는 영향을 받지 않는다. (테스트 독립성)
@DisplayName("게시글 컨버터 단위 테스트")
@ExtendWith(MockKExtension::class)
class PostConverterTest {

    // MockK: Kotlin 친화적인 Mocking 라이브러리
    // Mockito와 달리 Kotlin의 final class를 별도 설정 없이 Mock할 수 있다.
    private val userConverter: UserConverter = mockk()
    private val tagConverter: TagConverter = mockk()
    private val postImageConverter: PostImageConverter = mockk()

    // 테스트 대상(SUT: System Under Test)은 직접 생성하고 Mock을 주입한다.
    private lateinit var postConverter: PostConverter

    @BeforeEach
    fun setUp() {
        postConverter = PostConverter(
            userConverter = userConverter,
            tagConverter = tagConverter,
            postImageConverter = postImageConverter
        )
    }

    @Test
    @DisplayName("CreatePostRequestDto를 Post 엔티티로 변환한다")
    fun `게시글_생성_요청_DTO를_엔티티로_변환`() {
        // given
        val request = DummyPost.toCreateRequestDto()

        // when: request → Post 변환은 하위 converter 의존 없이 단순 필드 매핑이다.
        val result = postConverter.convert(request)

        // then
        result.title shouldBe DummyPost.TITLE
        result.subTitle shouldBe DummyPost.SUB_TITLE
        result.content shouldBe DummyPost.CONTENT
    }

    @Test
    @DisplayName("Post 엔티티를 PostResponseDto로 변환한다 (썸네일 없음)")
    fun `게시글_엔티티를_응답_DTO로_변환_썸네일_없음`() {
        // given
        val post = DummyPost.toEntity()
        val user = DummyUser.toEntity()
        val tag = DummyTag.toEntity()
        val postTag = DummyPostTag.toEntity()

        // 연관관계 설정: post에 user와 tag를 연결
        post.setBy(user)
        postTag.setBy(post)
        postTag.setBy(tag)

        val userResponse = DummyUser.toResponseDto()
        val tagResponse = DummyTag.toResponseDto()

        // MockK의 every { } 블록: "이 메서드가 이 인자로 호출되면 이 값을 반환해라"
        // Mockito의 when(...).thenReturn(...)과 동일한 역할이지만 더 간결하다.
        every { userConverter.convert(user) } returns userResponse
        every { tagConverter.convert(tag) } returns tagResponse

        // when
        val result = postConverter.convert(post)

        // then
        result.id shouldBe DummyPost.ID
        result.writer shouldBe userResponse
        result.tags!!.shouldHaveSize(1)
        result.tags!!.first() shouldBe tagResponse
        result.title shouldBe DummyPost.TITLE
        result.subTitle shouldBe DummyPost.SUB_TITLE
        result.content shouldBe DummyPost.CONTENT
        result.thumbNailImage.shouldBeNull()
    }

    @Test
    @DisplayName("Post 엔티티를 PostResponseDto로 변환한다 (썸네일 있음)")
    fun `게시글_엔티티를_응답_DTO로_변환_썸네일_있음`() {
        // given
        val post = DummyPost.toEntity()
        val user = DummyUser.toEntity()
        val postImage = DummyPostImage.toEntity()

        post.setBy(user)
        // PostImage.setBy(post)는 내부에서 post.addImage(this)를 호출한다.
        postImage.setBy(post)

        val userResponse = DummyUser.toResponseDto()
        val imageResponse = DummyPostImage.toResponseDto()

        every { userConverter.convert(user) } returns userResponse
        // post.postTags가 비어있으므로 tagConverter.convert()는 호출되지 않는다.
        every { postImageConverter.convert(postImage) } returns imageResponse

        // when
        val result = postConverter.convert(post)

        // then
        result.thumbNailImage.shouldNotBeNull()
        result.thumbNailImage shouldBe imageResponse
    }

    @Test
    @DisplayName("Post 엔티티 목록을 PostResponseDto 목록으로 변환한다")
    fun `게시글_엔티티_목록을_응답_DTO_목록으로_변환`() {
        // given
        val post1 = DummyPost.toEntity()
        val post2 = DummyPost.toEntity().also {
            it.id = 2L
        }
        val user = DummyUser.toEntity()

        post1.setBy(user)
        post2.setBy(user)

        val userResponse = DummyUser.toResponseDto()

        // any<User>()처럼 타입을 명시해야 오버로드 충돌을 피할 수 있다.
        // UserConverter.convert()는 User / CreateUserRequestDto / List<User> 세 가지 오버로드가 있어
        // any()만으로는 컴파일러가 어느 오버로드인지 추론하지 못한다.
        every { userConverter.convert(any<User>()) } returns userResponse

        // when
        val results = postConverter.convert(listOf(post1, post2))

        // then
        results shouldHaveSize 2
    }
}
