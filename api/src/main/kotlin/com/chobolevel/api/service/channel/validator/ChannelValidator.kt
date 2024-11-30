package com.chobolevel.api.service.channel.validator

import com.chobolevel.api.dto.channel.UpdateChannelRequestDto
import com.chobolevel.domain.entity.channel.ChannelUpdateMask
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ChannelValidator {

    fun validateWhenUpdate(request: UpdateChannelRequestDto) {
        request.updateMask.forEach {
            when (it) {
                ChannelUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 채널명이 유효하지 않습니다."
                        )
                    }
                }
                ChannelUpdateMask.USERS -> {
                    if (request.userIds == null) {
                        throw ApiException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            status = HttpStatus.BAD_REQUEST,
                            message = "변경할 채널 참여자가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
