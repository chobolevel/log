package com.chobolevel.api.guest.validator

import com.chobolevel.api.guest.dto.UpdateGuestBookRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.guest.vo.GuestBookUpdateMask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class GuestBookParameterValidatorTest : BehaviorSpec({

    val validator: GuestBookParameterValidator = GuestBookParameterValidator()

    given("방문록 수정 요청 파라미터를 검증할 때") {

        `when`("CONTENT 마스크인데 content가 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdateGuestBookRequest = UpdateGuestBookRequest(
                    password = "password123!",
                    content = null,
                    updateMask = listOf(GuestBookUpdateMask.CONTENT)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("CONTENT 마스크인데 content가 비어 있으면") {
            then("ApiException이 발생한다") {
                val request: UpdateGuestBookRequest = UpdateGuestBookRequest(
                    password = "password123!",
                    content = "",
                    updateMask = listOf(GuestBookUpdateMask.CONTENT)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("CONTENT 마스크이고 content가 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdateGuestBookRequest = UpdateGuestBookRequest(
                    password = "password123!",
                    content = "수정된 방문록 내용",
                    updateMask = listOf(GuestBookUpdateMask.CONTENT)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
