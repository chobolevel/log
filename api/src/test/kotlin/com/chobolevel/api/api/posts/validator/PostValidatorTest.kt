package com.chobolevel.api.api.posts.validator

import com.chobolevel.api.post.dto.UpdatePostRequestDto
import com.chobolevel.api.post.validator.PostParameterValidator
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.post.PostUpdateMask
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// [Validator 테스트 특징]
// - PostParameterValidator는 Spring Bean이지만 생성자 파라미터가 없으므로 직접 인스턴스화할 수 있다.
// - Spring 컨텍스트 없이 순수 단위 테스트로 작성 가능하다.
// - 각 updateMask 케이스별로 '예외 발생'과 '정상 통과' 두 가지를 반드시 검증한다.
@DisplayName("게시글 파라미터 검증 단위 테스트")
class PostValidatorTest {

    // Spring 의존성이 없으므로 직접 생성
    private val validator = PostParameterValidator()

    // --- TAGS ---

    @Test
    @DisplayName("TAGS mask - tagIds가 null이면 예외가 발생한다")
    fun `TAGS_mask_tagIds_null_예외`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TAGS)
        )

        // when & then
        // shouldThrow는 해당 예외가 발생하는지 검증하고 예외 객체를 반환한다.
        val ex = shouldThrow<ApiException> { validator.validate(request) }
        ex.message shouldBe "변경할 게시글 태그가 유효하지 않거나 최소 1개 이상이어야 합니다."
    }

    @Test
    @DisplayName("TAGS mask - tagIds가 빈 리스트이면 예외가 발생한다")
    fun `TAGS_mask_tagIds_빈_리스트_예외`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = emptyList(),
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TAGS)
        )

        // when & then
        shouldThrow<ApiException> { validator.validate(request) }
    }

    @Test
    @DisplayName("TAGS mask - tagIds가 유효하면 예외가 발생하지 않는다")
    fun `TAGS_mask_tagIds_유효_정상_통과`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = listOf(1L),
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TAGS)
        )

        // when & then
        // shouldNotThrowAny: 어떤 예외도 발생하지 않아야 한다.
        shouldNotThrowAny { validator.validate(request) }
    }

    // --- TITLE ---

    @Test
    @DisplayName("TITLE mask - title이 null이면 예외가 발생한다")
    fun `TITLE_mask_title_null_예외`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TITLE)
        )

        // when & then
        val ex = shouldThrow<ApiException> { validator.validate(request) }
        ex.message shouldBe "변경할 게시글 제목이 유효하지 않습니다."
    }

    @Test
    @DisplayName("TITLE mask - title이 빈 문자열이면 예외가 발생한다")
    fun `TITLE_mask_title_빈_문자열_예외`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = "",
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TITLE)
        )

        // when & then
        shouldThrow<ApiException> { validator.validate(request) }
    }

    @Test
    @DisplayName("TITLE mask - title이 유효하면 예외가 발생하지 않는다")
    fun `TITLE_mask_title_유효_정상_통과`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = "유효한 제목",
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TITLE)
        )

        // when & then
        shouldNotThrowAny { validator.validate(request) }
    }

    // --- SUB_TITLE ---

    @Test
    @DisplayName("SUB_TITLE mask - subTitle이 null이면 예외가 발생한다")
    fun `SUB_TITLE_mask_subTitle_null_예외`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.SUB_TITLE)
        )

        // when & then
        val ex = shouldThrow<ApiException> { validator.validate(request) }
        ex.message shouldBe "변경할 게시글 부제목이 유효하지 않습니다."
    }

    @Test
    @DisplayName("SUB_TITLE mask - subTitle이 유효하면 예외가 발생하지 않는다")
    fun `SUB_TITLE_mask_subTitle_유효_정상_통과`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = "유효한 부제목",
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.SUB_TITLE)
        )

        // when & then
        shouldNotThrowAny { validator.validate(request) }
    }

    // --- CONTENT ---

    @Test
    @DisplayName("CONTENT mask - content가 null이면 예외가 발생한다")
    fun `CONTENT_mask_content_null_예외`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.CONTENT)
        )

        // when & then
        val ex = shouldThrow<ApiException> { validator.validate(request) }
        ex.message shouldBe "변경할 게시글 내용이 유효하지 않습니다."
    }

    @Test
    @DisplayName("CONTENT mask - content가 유효하면 예외가 발생하지 않는다")
    fun `CONTENT_mask_content_유효_정상_통과`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = null,
            content = "유효한 내용",
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.CONTENT)
        )

        // when & then
        shouldNotThrowAny { validator.validate(request) }
    }

    // --- THUMB_NAIL_IMAGE ---

    @Test
    @DisplayName("THUMB_NAIL_IMAGE mask - 별도 검증 없이 항상 통과한다")
    fun `THUMB_NAIL_IMAGE_mask_항상_통과`() {
        // given: thumbNailImage가 null이어도 validator는 이 케이스를 검증하지 않는다.
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = null,
            subTitle = null,
            content = null,
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.THUMB_NAIL_IMAGE)
        )

        // when & then
        shouldNotThrowAny { validator.validate(request) }
    }

    // --- 복합 mask ---

    @Test
    @DisplayName("여러 mask를 함께 사용할 때 유효하지 않은 항목이 하나라도 있으면 예외가 발생한다")
    fun `복합_mask_하나라도_유효하지_않으면_예외`() {
        // given: TITLE은 유효하지만 CONTENT는 null
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = "유효한 제목",
            subTitle = null,
            content = null, // 유효하지 않음
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TITLE, PostUpdateMask.CONTENT)
        )

        // when & then
        shouldThrow<ApiException> { validator.validate(request) }
    }

    @Test
    @DisplayName("여러 mask를 함께 사용할 때 모두 유효하면 예외가 발생하지 않는다")
    fun `복합_mask_모두_유효_정상_통과`() {
        // given
        val request = UpdatePostRequestDto(
            tagIds = null,
            title = "유효한 제목",
            subTitle = "유효한 부제목",
            content = "유효한 내용",
            thumbNailImage = null,
            updateMask = listOf(PostUpdateMask.TITLE, PostUpdateMask.SUB_TITLE, PostUpdateMask.CONTENT)
        )

        // when & then
        shouldNotThrowAny { validator.validate(request) }
    }
}
