package com.chobolevel.api.guest.validator

import com.chobolevel.api.guest.dto.UpdateGuestBookRequestDto
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.guest.vo.GuestBookUpdateMask
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class GuestBookParameterValidator {

    fun validate(request: UpdateGuestBookRequestDto) {
        request.updateMask.forEach {
            when (it) {
                GuestBookUpdateMask.CONTENT -> {
                    if (request.content.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 방멸록 내용이 올바르지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
