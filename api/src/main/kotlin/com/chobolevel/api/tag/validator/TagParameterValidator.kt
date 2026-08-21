package com.chobolevel.api.tag.validator

import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.tag.vo.TagUpdateMask
import org.springframework.stereotype.Component

@Component
class TagParameterValidator {

    fun validate(request: UpdateTagRequest) {
        request.updateMask.forEach {
            when (it) {
                TagUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 태그 이름 파라미터가 유효하지 않습니다."
                        )
                    }
                }

                TagUpdateMask.ORDER -> {
                    if (request.order == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 태그 순서 파라미터가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
