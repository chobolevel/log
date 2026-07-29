package com.chobolevel.api.post.comment.validator

import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.post.comment.vo.PostCommentUpdateMask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class PostCommentParameterValidatorTest : BehaviorSpec({

    val validator: PostCommentParameterValidator = PostCommentParameterValidator()

    given("댓글 수정 요청 파라미터를 검증할 때") {

        `when`("CONTENT 마스크인데 content가 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostCommentRequest = UpdatePostCommentRequest(
                    content = null,
                    updateMask = listOf(PostCommentUpdateMask.CONTENT)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("CONTENT 마스크인데 content가 비어 있으면") {
            then("ApiException이 발생한다") {
                val request: UpdatePostCommentRequest = UpdatePostCommentRequest(
                    content = "",
                    updateMask = listOf(PostCommentUpdateMask.CONTENT)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("CONTENT 마스크이고 content가 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdatePostCommentRequest = UpdatePostCommentRequest(
                    content = "수정된 댓글 내용",
                    updateMask = listOf(PostCommentUpdateMask.CONTENT)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
