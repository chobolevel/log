package com.chobolevel.api.tag.validator

import com.chobolevel.api.tag.dto.UpdateTagRequestDto
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.tag.entity.TagUpdateMask
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class TagParameterValidator {

    fun validate(request: UpdateTagRequestDto) {
        request.updateMask.forEach {
            when (it) {
                TagUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 태그 이름 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                TagUpdateMask.ORDER -> {
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
