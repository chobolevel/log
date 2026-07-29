package com.chobolevel.api.upload.validator

import com.chobolevel.api.upload.dto.UploadRequest
import com.chobolevel.domain.common.exception.ApiException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class UploadValidatorTest : BehaviorSpec({

    val validator: UploadValidator = UploadValidator()

    given("업로드 요청 파라미터를 검증할 때") {

        `when`("지원하지 않는 prefix이면") {
            then("ApiException이 발생한다") {
                val request: UploadRequest = UploadRequest(
                    prefix = "video",
                    filename = "test",
                    extension = "jpg"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("지원하지 않는 extension이면") {
            then("ApiException이 발생한다") {
                val request: UploadRequest = UploadRequest(
                    prefix = "image",
                    filename = "test",
                    extension = "pdf"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("prefix와 extension이 모두 유효하면") {
            then("예외 없이 통과한다") {
                val request: UploadRequest = UploadRequest(
                    prefix = "image",
                    filename = "test",
                    extension = "jpg"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("extension이 png이면") {
            then("예외 없이 통과한다") {
                val request: UploadRequest = UploadRequest(
                    prefix = "image",
                    filename = "test",
                    extension = "png"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("extension이 gif이면") {
            then("예외 없이 통과한다") {
                val request: UploadRequest = UploadRequest(
                    prefix = "image",
                    filename = "test",
                    extension = "gif"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
