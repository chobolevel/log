package com.chobolevel.api.user.validator

import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserUpdateMask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UserBusinessValidatorTest : BehaviorSpec({

    val userRepository: UserRepository = mockk()
    val passwordProvider: PasswordProvider = mockk()
    val validator: UserBusinessValidator = UserBusinessValidator(
        userRepository = userRepository,
        passwordProvider = passwordProvider
    )

    // BehaviorSpec에서 given/when 블록 내 코드는 스펙 초기화 시 한 번만 실행되므로
    // every { } 등 mock 설정은 반드시 then 블록 안에 두어야 한다.
    beforeEach {
        clearAllMocks()
    }

    given("회원가입 요청을 검증할 때") {

        `when`("이메일이 이미 존재하면") {
            then("USER_EMAIL_ALREADY_EXISTS 예외가 발생한다") {
                // given
                val request: CreateUserRequest = CreateUserRequest(
                    email = DummyUser.EMAIL,
                    password = "password123!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = DummyUser.NICKNAME
                )
                every { userRepository.existsByEmail(DummyUser.EMAIL) } returns true

                // when
                val exception: ApiException = shouldThrow<ApiException> {
                    validator.validate(request)
                }

                // then
                exception.errorCode shouldBe ErrorCode.USER_EMAIL_ALREADY_EXISTS
            }
        }

        `when`("이메일은 없지만 닉네임이 이미 존재하면") {
            then("USER_NICKNAME_ALREADY_EXISTS 예외가 발생한다") {
                // given
                val request: CreateUserRequest = CreateUserRequest(
                    email = DummyUser.EMAIL,
                    password = "password123!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = DummyUser.NICKNAME
                )
                every { userRepository.existsByEmail(DummyUser.EMAIL) } returns false
                every { userRepository.existsByNickname(DummyUser.NICKNAME) } returns true

                // when
                val exception: ApiException = shouldThrow<ApiException> {
                    validator.validate(request)
                }

                // then
                exception.errorCode shouldBe ErrorCode.USER_NICKNAME_ALREADY_EXISTS
            }
        }

        `when`("이메일과 닉네임 모두 사용 가능하면") {
            then("예외 없이 통과한다") {
                // given
                val request: CreateUserRequest = CreateUserRequest(
                    email = DummyUser.EMAIL,
                    password = "password123!",
                    socialId = null,
                    loginType = UserLoginType.GENERAL,
                    nickname = DummyUser.NICKNAME
                )
                every { userRepository.existsByEmail(DummyUser.EMAIL) } returns false
                every { userRepository.existsByNickname(DummyUser.NICKNAME) } returns false

                // when + then
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("회원 수정 요청을 검증할 때") {

        `when`("updateMask에 NICKNAME이 포함되지 않으면") {
            then("닉네임 중복 검사를 수행하지 않는다") {
                // given
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = null,
                    updateMask = emptyList()
                )

                // when + then
                shouldNotThrow<Exception> { validator.validate(request) }
                verify(exactly = 0) { userRepository.existsByNickname(any()) }
            }
        }

        `when`("updateMask에 NICKNAME이 포함되고 닉네임이 이미 존재하면") {
            then("USER_NICKNAME_ALREADY_EXISTS 예외가 발생한다") {
                // given
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = DummyUser.NICKNAME,
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )
                every { userRepository.existsByNickname(DummyUser.NICKNAME) } returns true

                // when
                val exception: ApiException = shouldThrow<ApiException> {
                    validator.validate(request)
                }

                // then
                exception.errorCode shouldBe ErrorCode.USER_NICKNAME_ALREADY_EXISTS
            }
        }

        `when`("updateMask에 NICKNAME이 포함되고 닉네임을 사용할 수 있으면") {
            then("예외 없이 통과한다") {
                // given
                val request: UpdateUserRequest = UpdateUserRequest(
                    nickname = DummyUser.NICKNAME,
                    updateMask = listOf(UserUpdateMask.NICKNAME)
                )
                every { userRepository.existsByNickname(DummyUser.NICKNAME) } returns false

                // when + then
                shouldNotThrow<Exception> { validator.validate(request) }
            }
        }
    }

    given("비밀번호 변경 요청을 검증할 때") {

        `when`("현재 비밀번호가 일치하지 않으면") {
            then("USER_PASSWORD_NOT_MATCH 예외가 발생한다") {
                // given
                val user: User = DummyUser.toEntity()
                val request: ChangeUserPasswordRequest = ChangeUserPasswordRequest(
                    curPassword = "wrongPassword",
                    newPassword = "newPassword!"
                )
                every {
                    passwordProvider.matches(plainText = request.curPassword, encodedText = user.password)
                } returns false

                // when
                val exception: ApiException = shouldThrow<ApiException> {
                    validator.validate(user, request)
                }

                // then
                exception.errorCode shouldBe ErrorCode.USER_PASSWORD_NOT_MATCH
            }
        }

        `when`("현재 비밀번호는 맞지만 새 비밀번호가 현재와 동일하면") {
            then("USER_PASSWORD_REUSE_NOT_ALLOWED 예외가 발생한다") {
                // given
                val user: User = DummyUser.toEntity()
                val request: ChangeUserPasswordRequest = ChangeUserPasswordRequest(
                    curPassword = "samePassword!",
                    newPassword = "samePassword!"
                )
                every {
                    passwordProvider.matches(plainText = any(), encodedText = user.password)
                } returns true

                // when
                val exception: ApiException = shouldThrow<ApiException> {
                    validator.validate(user, request)
                }

                // then
                exception.errorCode shouldBe ErrorCode.USER_PASSWORD_REUSE_NOT_ALLOWED
            }
        }

        `when`("현재 비밀번호가 맞고 새 비밀번호가 현재와 다르면") {
            then("예외 없이 통과한다") {
                // given
                val user: User = DummyUser.toEntity()
                val request: ChangeUserPasswordRequest = ChangeUserPasswordRequest(
                    curPassword = "currentPassword!",
                    newPassword = "newPassword!"
                )
                every {
                    passwordProvider.matches(plainText = request.curPassword, encodedText = user.password)
                } returns true
                every {
                    passwordProvider.matches(plainText = request.newPassword, encodedText = user.password)
                } returns false

                // when + then
                shouldNotThrow<Exception> { validator.validate(user, request) }
            }
        }
    }
})
