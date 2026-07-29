package com.chobolevel.api.auth.validator

import com.chobolevel.api.auth.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.auth.dto.LoginRequest
import com.chobolevel.api.auth.dto.SendEmailVerificationCodeRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.user.vo.UserLoginType
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class AuthParameterValidatorTest : BehaviorSpec({

    val validator: AuthParameterValidator = AuthParameterValidator()

    given("로그인 요청 파라미터를 검증할 때") {

        `when`("GENERAL 타입인데 password가 null이면") {
            then("ApiException이 발생한다") {
                val request: LoginRequest = LoginRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = null,
                    loginType = UserLoginType.GENERAL
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("GENERAL 타입인데 password가 비어 있으면") {
            then("ApiException이 발생한다") {
                val request: LoginRequest = LoginRequest(
                    email = "test@test.com",
                    password = "",
                    socialId = null,
                    loginType = UserLoginType.GENERAL
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("GENERAL 타입이고 password가 있으면") {
            then("예외 없이 통과한다") {
                val request: LoginRequest = LoginRequest(
                    email = "test@test.com",
                    password = "Pass1234!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("KAKAO 타입인데 socialId가 null이면") {
            then("ApiException이 발생한다") {
                val request: LoginRequest = LoginRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = null,
                    loginType = UserLoginType.KAKAO
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("KAKAO 타입이고 socialId가 있으면") {
            then("예외 없이 통과한다") {
                val request: LoginRequest = LoginRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = "kakao_12345",
                    loginType = UserLoginType.KAKAO
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("이메일 인증 코드 발송 요청 파라미터를 검증할 때") {

        `when`("이메일 형식이 올바르지 않으면") {
            then("ApiException이 발생한다") {
                val request: SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(
                    email = "not-an-email"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("이메일 형식이 올바르면") {
            then("예외 없이 통과한다") {
                val request: SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(
                    email = "test@test.com"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("이메일 인증 코드 확인 요청 파라미터를 검증할 때") {

        `when`("이메일 형식이 올바르지 않으면") {
            then("ApiException이 발생한다") {
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = "not-an-email",
                    verificationCode = "1234567890123"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("인증 코드가 13자리가 아니면") {
            then("ApiException이 발생한다") {
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = "test@test.com",
                    verificationCode = "12345"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("이메일 형식이 올바르고 인증 코드가 13자리이면") {
            then("예외 없이 통과한다") {
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = "test@test.com",
                    verificationCode = "1234567890123"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
