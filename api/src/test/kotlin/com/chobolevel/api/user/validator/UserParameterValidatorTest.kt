package com.chobolevel.api.user.validator

import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserUpdateMask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec

class UserParameterValidatorTest : BehaviorSpec({

    val validator: UserParameterValidator = UserParameterValidator()

    given("회원가입 요청 파라미터를 검증할 때") {

        `when`("이메일 형식이 올바르지 않으면") {
            then("ApiException이 발생한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "not-an-email",
                    password = "Pass1234!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("닉네임에 숫자가 포함되면") {
            then("ApiException이 발생한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = "Pass1234!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "nick123"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("GENERAL 타입인데 비밀번호가 규칙에 맞지 않으면") {
            then("ApiException이 발생한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = "tooshort",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("이메일, 닉네임, 비밀번호가 모두 유효하면") {
            then("예외 없이 통과한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = "Pass1234!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = "홍길동"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("KAKAO 타입이면 비밀번호 검증을 건너뛴다") {
            then("예외 없이 통과한다") {
                val request: CreateUserRequest = CreateUserRequest(
                    email = "test@test.com",
                    password = null,
                    socialId = "kakaoSocialId",
                    loginType = UserLoginType.KAKAO,
                    nickname = "홍길동"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("회원 수정 요청 파라미터를 검증할 때") {

        `when`("updateMask에 NICKNAME이 없으면") {
            then("예외 없이 통과한다") {
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = null,
                    updateMask = emptyList()
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }

        `when`("NICKNAME 마스크인데 닉네임이 null이면") {
            then("ApiException이 발생한다") {
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = null,
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("NICKNAME 마스크인데 닉네임에 숫자가 포함되면") {
            then("ApiException이 발생한다") {
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = "nick123",
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("NICKNAME 마스크이고 닉네임이 유효하면") {
            then("예외 없이 통과한다") {
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = "새닉네임",
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("비밀번호 변경 요청 파라미터를 검증할 때") {

        `when`("새 비밀번호가 규칙에 맞지 않으면") {
            then("ApiException이 발생한다") {
                val request: ChangeUserPasswordRequest = ChangeUserPasswordRequest(
                    curPassword = "oldPass1!",
                    newPassword = "short"
                )
                shouldThrow<ApiException> { validator.validate(request) }
            }
        }

        `when`("새 비밀번호가 유효하면") {
            then("예외 없이 통과한다") {
                val request: ChangeUserPasswordRequest = ChangeUserPasswordRequest(
                    curPassword = "oldPass1!",
                    newPassword = "NewPass1234!"
                )
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }
})
