package com.chobolevel.api.service.user.validator

import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UpdateUserValidator : UpdateUserValidatable {

    private val phoneRegexp = "^\\d{10,11}$".toRegex()

    override fun validate(request: UpdateUserRequestDto) {
        if (request.nickname.isNullOrEmpty()) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "변경할 닉네임이 유효하지 않습니다."
            )
        }
        if (request.phone.isNullOrEmpty()) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "변경할 전화번호가 유효하지 않습니다."
            )
        }
        if (phoneRegexp.matches(request.phone)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "변경할 전화번호는 10-11자리 숫자만 가능합니다."
            )
        }
    }
}
