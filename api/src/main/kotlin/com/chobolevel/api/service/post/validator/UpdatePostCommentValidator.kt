package com.chobolevel.api.service.post.validator

import com.chobolevel.api.dto.post.comment.UpdatePostCommentRequestDto
import com.chobolevel.domain.entity.post.comment.PostCommentUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.stereotype.Component

@Component
class UpdatePostCommentValidator : UpdatePostCommentValidatable {

    override fun validate(request: UpdatePostCommentRequestDto) {
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
