package com.chobolevel.api.post.validator

import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.post.vo.PostUpdateMask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class PostParameterValidatorTest : BehaviorSpec({

    val validator: PostParameterValidator = PostParameterValidator()

    given("게시글 수정 요청 파라미터를 검증할 때") {

        `when`("TAGS 마스크인데 tagIds가 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TAGS)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("TAGS 마스크인데 tagIds가 비어 있으면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = emptyList(),
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TAGS)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("TAGS 마스크이고 tagIds가 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = listOf(1L, 2L),
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TAGS)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("TITLE 마스크인데 title이 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TITLE)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("TITLE 마스크이고 title이 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = "새 제목",
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.TITLE)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("SUB_TITLE 마스크인데 subTitle이 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.SUB_TITLE)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("SUB_TITLE 마스크이고 subTitle이 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = "새 부제목",
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.SUB_TITLE)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("CONTENT 마스크인데 content가 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = null,
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.CONTENT)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("CONTENT 마스크이고 content가 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdatePostRequest = UpdatePostRequest(
                    tagIds = null,
                    title = null,
                    subTitle = null,
                    content = "새 내용",
                    thumbnailImage = null,
                    updateMask = listOf(PostUpdateMask.CONTENT)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
