package com.chobolevel.api.tag.validator

import com.chobolevel.api.tag.dto.UpdateTagRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.tag.vo.TagUpdateMask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class TagParameterValidatorTest : BehaviorSpec({

    val validator: TagParameterValidator = TagParameterValidator()

    given("태그 수정 요청 파라미터를 검증할 때") {

        `when`("NAME 마스크인데 name이 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = null,
                    order = null,
                    updateMask = listOf(TagUpdateMask.NAME)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("NAME 마스크인데 name이 비어 있으면") {
            then("ApiException이 발생한다") {
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = "",
                    order = null,
                    updateMask = listOf(TagUpdateMask.NAME)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("NAME 마스크이고 name이 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = "newTagName",
                    order = null,
                    updateMask = listOf(TagUpdateMask.NAME)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("ORDER 마스크인데 order가 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = null,
                    order = null,
                    updateMask = listOf(TagUpdateMask.ORDER)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("ORDER 마스크이고 order가 있으면") {
            then("예외 없이 통과한다") {
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = null,
                    order = 2,
                    updateMask = listOf(TagUpdateMask.ORDER)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("NAME과 ORDER 마스크가 모두 있고 둘 다 유효하면") {
            then("예외 없이 통과한다") {
                val request: UpdateTagRequest = UpdateTagRequest(
                    name = "newTagName",
                    order = 2,
                    updateMask = listOf(TagUpdateMask.NAME, TagUpdateMask.ORDER)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
