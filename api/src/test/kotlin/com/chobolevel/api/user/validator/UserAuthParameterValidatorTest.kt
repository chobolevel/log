package com.chobolevel.api.user.validator

import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.LoginRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.SocialLoginRequest
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.user.vo.UserLoginType
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class UserAuthParameterValidatorTest : BehaviorSpec({

    val validator: UserAuthParameterValidator = UserAuthParameterValidator()

    given("일반 로그인 요청 파라미터를 검증할 때") {

        `when`("이메일 형식이 올바르지 않으면") {
            then("ApiException이 발생한다") {
                val request: LoginRequest = LoginRequest(
                    email = "not-an-email",
                    password = "Pass1234!"
                )
                shouldThrow<InvalidParameterException> { validator.validate(request) }
            }
        }

        `when`("이메일 형식이 올바르면") {
            then("예외 없이 통과한다") {
                val request: LoginRequest = LoginRequest(
                    email = "test@test.com",
                    password = "Pass1234!"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("소셜 로그인 요청 파라미터를 검증할 때") {

        `when`("이메일 형식이 올바르지 않으면") {
            then("ApiException이 발생한다") {
                val request: SocialLoginRequest = SocialLoginRequest(
                    email = "not-an-email",
                    socialId = "github_12345",
                    loginType = UserLoginType.GITHUB,
                    nickname = "홍길동"
                )
                shouldThrow<InvalidParameterException> { validator.validate(request) }
            }
        }

        `when`("loginType이 GENERAL이면") {
            then("ApiException이 발생한다") {
                val request: SocialLoginRequest = SocialLoginRequest(
                    email = "test@test.com",
                    socialId = "github_12345",
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                shouldThrow<InvalidParameterException> { validator.validate(request) }
            }
        }

        `when`("이메일 형식이 올바르고 loginType이 소셜 타입이면") {
            then("예외 없이 통과한다") {
                val request: SocialLoginRequest = SocialLoginRequest(
                    email = "test@test.com",
                    socialId = "github_12345",
                    loginType = UserLoginType.GITHUB,
                    nickname = "홍길동"
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
                shouldThrow<InvalidParameterException> { validator.validate(request) }
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
                shouldThrow<InvalidParameterException> { validator.validate(request) }
            }
        }

        `when`("인증 코드가 13자리가 아니면") {
            then("ApiException이 발생한다") {
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = "test@test.com",
                    verificationCode = "12345"
                )
                shouldThrow<InvalidParameterException> { validator.validate(request) }
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
