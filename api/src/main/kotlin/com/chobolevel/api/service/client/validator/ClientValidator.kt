package com.chobolevel.api.service.client.validator

import com.chobolevel.api.dto.client.UpdateClientRequestDto
import com.chobolevel.domain.entity.client.ClientUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ClientValidator {

    fun validateWhenUpdate(request: UpdateClientRequestDto) {
        request.updateMask.forEach {
            when (it) {
                ClientUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 이름이 유효하지 않습니다."
                        )
                    }
                }
                ClientUpdateMask.REDIRECT_URL -> {
                    if (request.redirectUrl.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 redirect_url이 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
