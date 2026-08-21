package com.chobolevel.api.guest.validator

import com.chobolevel.api.guest.dto.UpdateGuestBookRequest
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.guest.vo.GuestBookUpdateMask
import org.springframework.stereotype.Component

@Component
class GuestBookParameterValidator {

    fun validate(request: UpdateGuestBookRequest) {
        request.updateMask.forEach {
            when (it) {
                GuestBookUpdateMask.CONTENT -> {
                    if (request.content.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 방멸록 내용이 올바르지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
