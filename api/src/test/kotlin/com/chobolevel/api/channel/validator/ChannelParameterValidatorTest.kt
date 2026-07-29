package com.chobolevel.api.channel.validator

import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.domain.channel.vo.ChannelUpdateMask
import com.chobolevel.domain.common.exception.ApiException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class ChannelParameterValidatorTest : BehaviorSpec({

    val validator: ChannelParameterValidator = ChannelParameterValidator()

    given("채널 수정 요청 파라미터를 검증할 때") {

        `when`("NAME 마스크인데 name이 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = null,
                    userIds = null,
                    updateMask = listOf(ChannelUpdateMask.NAME)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("NAME 마스크인데 name이 비어 있으면") {
            then("ApiException이 발생한다") {
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = "",
                    userIds = null,
                    updateMask = listOf(ChannelUpdateMask.NAME)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("NAME 마스크이고 name이 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = "새 채널명",
                    userIds = null,
                    updateMask = listOf(ChannelUpdateMask.NAME)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("USERS 마스크인데 userIds가 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = null,
                    userIds = null,
                    updateMask = listOf(ChannelUpdateMask.USERS)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("USERS 마스크이고 userIds가 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdateChannelRequest = UpdateChannelRequest(
                    name = null,
                    userIds = listOf(1L, 2L),
                    updateMask = listOf(ChannelUpdateMask.USERS)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
