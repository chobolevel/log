package com.chobolevel.api.service.guest.validator

import com.chobolevel.api.dto.guest.UpdateGuestBookRequestDto
import com.chobolevel.domain.entity.guest.GuestBookUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UpdateGuestBookValidator : UpdateGuestBookValidatable {

    override fun validate(request: UpdateGuestBookRequestDto) {
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
