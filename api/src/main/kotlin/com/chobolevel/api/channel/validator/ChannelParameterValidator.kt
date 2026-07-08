package com.chobolevel.api.channel.validator

import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ChannelParameterValidator {

    fun validate(request: UpdateChannelRequest) {
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
