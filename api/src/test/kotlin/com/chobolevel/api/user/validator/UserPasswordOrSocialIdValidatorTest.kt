package com.chobolevel.api.user.validator

import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.domain.user.vo.UserLoginType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UserPasswordOrSocialIdValidatorTest : BehaviorSpec({

    val validator: UserPasswordOrSocialIdValidator = UserPasswordOrSocialIdValidator()

    given("GENERAL 로그인 타입일 때") {

        `when`("password가 null이면") {
            then("false를 반환한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                validator.isValid(request, null) shouldBe false
            }
        }

        `when`("password가 비어 있으면") {
            then("false를 반환한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = "",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                validator.isValid(request, null) shouldBe false
            }
        }

        `when`("password가 있으면") {
            then("true를 반환한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = "Pass1234!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                validator.isValid(request, null) shouldBe true
            }
        }
    }

    given("소셜 로그인 타입(KAKAO)일 때") {

        `when`("socialId가 null이면") {
            then("false를 반환한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = null,
                    loginType = UserLoginType.KAKAO,
                    nickname = "홍길동"
                )
                validator.isValid(request, null) shouldBe false
            }
        }

        `when`("socialId가 있으면") {
            then("true를 반환한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = "kakao_12345",
                    loginType = UserLoginType.KAKAO,
                    nickname = "홍길동"
                )
                validator.isValid(request, null) shouldBe true
            }
        }
    }
})
