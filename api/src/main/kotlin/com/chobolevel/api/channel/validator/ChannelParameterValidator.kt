package com.chobolevel.api.channel.validator

import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import org.springframework.stereotype.Component

@Component
class ChannelParameterValidator {

    fun validate(request: UpdateChannelRequest) {
        request.updateMask.forEach {
            when (it) {
                ChannelUpdateMask.NAME -> {
                    if (request.name.isNullOrEmpty()) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 채널명이 유효하지 않습니다."
                        )
                    }
                }
                ChannelUpdateMask.USERS -> {
                    if (request.userIds == null) {
                        throw InvalidParameterException(
                            errorCode = ErrorCode.INVALID_PARAMETER,
                            message = "변경할 채널 참여자가 유효하지 않습니다."
                        )
                    }
                }
            }
        }
    }
}
