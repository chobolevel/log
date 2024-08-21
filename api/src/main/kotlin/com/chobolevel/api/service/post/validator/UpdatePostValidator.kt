package com.chobolevel.api.service.post.validator

import com.chobolevel.api.dto.post.UpdatePostRequestDto
import com.chobolevel.domain.entity.post.PostUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import jakarta.persistence.PostUpdate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UpdatePostValidator : UpdatePostValidatable {

    override fun validate(request: UpdatePostRequestDto) {
        request.updateMask.forEach {
            when (it) {
                PostUpdateMask.TAGS -> {
                    if (request.tagIds.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 게시글 태그가 유효하지 않거나 최소 1개 이상이어야 합니다."
                        )
                    }
                }

                PostUpdateMask.TITLE -> {
                    if (request.title.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 게시글 제목이 유효하지 않습니다."
                        )
                    }
                }

                PostUpdateMask.CONTENT -> {
                    if (request.content.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 게시글 내용이 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
