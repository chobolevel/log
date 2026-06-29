package com.chobolevel.api.post.comment.validator

import com.chobolevel.api.post.comment.dto.UpdatePostCommentRequestDto
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.comment.entity.PostCommentUpdateMask
import org.springframework.stereotype.Component

@Component
class PostCommentParameterValidator {

    fun validate(request: UpdatePostCommentRequestDto) {
        request.updateMask.forEach {
            when (it) {
                PostCommentUpdateMask.CONTENT -> {
                    if (request.content.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 댓글 내용이 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
