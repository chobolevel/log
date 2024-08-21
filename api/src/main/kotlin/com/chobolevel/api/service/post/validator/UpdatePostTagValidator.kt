package com.chobolevel.api.service.post.validator

import com.chobolevel.api.dto.post.UpdatePostTagRequestDto
import com.chobolevel.domain.entity.post.tag.PostTagUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UpdatePostTagValidator : UpdatePostTagValidatable {

    override fun validate(request: UpdatePostTagRequestDto) {
        request.updateMask.forEach {
            when (it) {
                PostTagUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 태그 이름 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                PostTagUpdateMask.ORDER -> {
                    if (request.order == null) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 태그 순서 파라미터가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
