package com.chobolevel.api.user.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dummy.DummyAuth
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.common.provider.RedisCacheProvider
import com.chobolevel.api.common.provider.ResendEmailProvider
import com.chobolevel.api.common.security.TokenProvider
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.JwtResponse
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.common.exception.BadCredentialException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.common.exception.UnAuthorizedException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import com.chobolevel.domain.user.vo.UserLoginType
import com.chobolevel.domain.user.vo.UserRoleType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.concurrent.TimeUnit

class UserAuthServiceTest : BehaviorSpec({

    val userRepository: UserRepository = mockk()
    val userConverter: UserConverter = mockk()
    val tokenProvider: TokenProvider = mockk()
    val passwordProvider: PasswordProvider = mockk()
    val cacheProvider: RedisCacheProvider = mockk()
    val emailProvider: ResendEmailProvider = mockk()
    val userBusinessValidator: UserBusinessValidator = mockk()
    val service: UserAuthService = UserAuthService(
        userRepository = userRepository,
        userConverter = userConverter,
        tokenProvider = tokenProvider,
        passwordProvider = passwordProvider,
        cacheProvider = cacheProvider,
        emailProvider = emailProvider,
        userBusinessValidator = userBusinessValidator
    )

    beforeEach {
        clearAllMocks()
    }

    given("일반 로그인 요청이 들어올 때") {
        `when`("이메일과 비밀번호가 유효하면") {
            then("JWT 토큰을 반환하고 Redis에 userId 키로 refresh token을 저장한다") {
                // given
                val request = DummyAuth.toGeneralLoginRequest()
                val user: User = DummyUser.toEntity()
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                every { userRepository.findByEmailOrNull(request.email) } returns user
                every { passwordProvider.matches(request.password, user.password) } returns true
                every { tokenProvider.generateTokenPair(any()) } returns jwtResponse
                justRun { cacheProvider.put(any(), any(), any(), any()) }

                // when
                val result: JwtResponse = service.login(request)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
                verify(exactly = 1) {
                    cacheProvider.put(
                        "${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}",
                        DummyAuth.REFRESH_TOKEN,
                        any(),
                        TimeUnit.MILLISECONDS
                    )
                }
            }
        }

        `when`("회원이 존재하지 않으면") {
            then("BadCredentialException이 발생한다") {
                // given
                val request = DummyAuth.toGeneralLoginRequest()
                every { userRepository.findByEmailOrNull(request.email) } returns null

                // when & then
                shouldThrow<BadCredentialException> {
                    service.login(request)
                }
            }
        }

        `when`("비밀번호가 일치하지 않으면") {
            then("BadCredentialException이 발생한다") {
                // given
                val request = DummyAuth.toGeneralLoginRequest()
                val user: User = DummyUser.toEntity()
                every { userRepository.findByEmailOrNull(request.email) } returns user
                every { passwordProvider.matches(request.password, user.password) } returns false

                // when & then
                shouldThrow<BadCredentialException> {
                    service.login(request)
                }
            }
        }
    }

    given("소셜 로그인 요청이 들어올 때") {
        `when`("이메일이 존재하지 않으면") {
            then("converter로 신규 유저를 생성하고 JWT 토큰을 반환한다") {
                // given
                val request = DummyAuth.toGithubSocialLoginRequest()
                val newUser: User = User(
                    email = DummyUser.EMAIL,
                    password = "",
                    socialId = DummyAuth.GITHUB_SOCIAL_ID,
                    loginType = UserLoginType.GITHUB,
                    nickname = DummyUser.NICKNAME,
                    role = UserRoleType.ROLE_USER
                ).also { it.id = DummyUser.ID }
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                every { userRepository.findByEmailOrNull(request.email) } returns null
                every { userConverter.convert(request) } returns newUser
                every { userRepository.save(newUser) } returns newUser
                every { tokenProvider.generateTokenPair(any()) } returns jwtResponse
                justRun { cacheProvider.put(any(), any(), any(), any()) }

                // when
                val result: JwtResponse = service.socialLogin(request)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
                verify(exactly = 1) { userConverter.convert(request) }
                verify(exactly = 1) {
                    cacheProvider.put(
                        "${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}",
                        DummyAuth.REFRESH_TOKEN,
                        any(),
                        TimeUnit.MILLISECONDS
                    )
                }
            }
        }

        `when`("이메일이 존재하고 소셜 아이디가 일치하면") {
            then("JWT 토큰을 반환하고 Redis에 userId 키로 refresh token을 저장한다") {
                // given
                val request = DummyAuth.toGithubSocialLoginRequest()
                val user: User = User(
                    email = DummyUser.EMAIL,
                    password = "",
                    socialId = DummyAuth.GITHUB_SOCIAL_ID,
                    loginType = UserLoginType.GITHUB,
                    nickname = DummyUser.NICKNAME,
                    role = UserRoleType.ROLE_USER
                ).also { it.id = DummyUser.ID }
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                every { userRepository.findByEmailOrNull(request.email) } returns user
                every { tokenProvider.generateTokenPair(any()) } returns jwtResponse
                justRun { cacheProvider.put(any(), any(), any(), any()) }

                // when
                val result: JwtResponse = service.socialLogin(request)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
                verify(exactly = 1) {
                    cacheProvider.put(
                        "${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}",
                        DummyAuth.REFRESH_TOKEN,
                        any(),
                        TimeUnit.MILLISECONDS
                    )
                }
            }
        }

        `when`("이메일이 존재하고 소셜 아이디가 변경되었으면") {
            then("소셜 아이디를 업데이트하고 JWT 토큰을 반환한다") {
                // given
                val request = DummyAuth.toGithubSocialLoginRequest()
                val user: User = User(
                    email = DummyUser.EMAIL,
                    password = "",
                    socialId = "old_social_id",
                    loginType = UserLoginType.GITHUB,
                    nickname = DummyUser.NICKNAME,
                    role = UserRoleType.ROLE_USER
                ).also { it.id = DummyUser.ID }
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                every { userRepository.findByEmailOrNull(request.email) } returns user
                every { tokenProvider.generateTokenPair(any()) } returns jwtResponse
                justRun { cacheProvider.put(any(), any(), any(), any()) }

                // when
                service.socialLogin(request)

                // then
                user.socialId shouldBe DummyAuth.GITHUB_SOCIAL_ID
            }
        }

        `when`("이메일이 GENERAL 타입으로 이미 가입되어 있으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request = DummyAuth.toGithubSocialLoginRequest()
                val user: User = DummyUser.toEntity()
                every { userRepository.findByEmailOrNull(request.email) } returns user

                // when & then
                shouldThrow<InvalidParameterException> {
                    service.socialLogin(request)
                }
            }
        }
    }

    given("토큰 갱신 요청이 들어올 때") {
        `when`("유효한 refresh token이고 Redis에 저장된 토큰과 일치하면") {
            then("새 JWT 토큰을 반환하고 Redis에 새 refresh token으로 교체한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                every { tokenProvider.validateToken(DummyAuth.REFRESH_TOKEN) } returns true
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                every { cacheProvider.get("${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}") } returns DummyAuth.REFRESH_TOKEN
                every { tokenProvider.generateTokenPair(authentication) } returns jwtResponse
                justRun { cacheProvider.put(any(), any(), any(), any()) }

                // when
                val result: JwtResponse = service.reissue(DummyAuth.REFRESH_TOKEN)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
                verify(exactly = 1) {
                    cacheProvider.put(
                        "${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}",
                        DummyAuth.REFRESH_TOKEN,
                        any(),
                        TimeUnit.MILLISECONDS
                    )
                }
            }
        }

        `when`("Redis에 저장된 refresh token이 없으면") {
            then("UnAuthorizedException이 발생한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                every { tokenProvider.validateToken(DummyAuth.REFRESH_TOKEN) } returns true
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                every { cacheProvider.get("${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}") } returns null

                // when & then
                shouldThrow<UnAuthorizedException> {
                    service.reissue(DummyAuth.REFRESH_TOKEN)
                }
            }
        }

        `when`("Redis에 저장된 토큰과 요청 토큰이 다르면") {
            then("UnAuthorizedException이 발생한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                every { tokenProvider.validateToken(DummyAuth.REFRESH_TOKEN) } returns true
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                every { cacheProvider.get("${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}") } returns "other.refresh.token"

                // when & then
                shouldThrow<UnAuthorizedException> {
                    service.reissue(DummyAuth.REFRESH_TOKEN)
                }
            }
        }
    }

    given("이메일 인증 코드 발송 요청이 들어올 때") {
        `when`("가입된 이메일이 주어지면") {
            then("Redis에 인증 코드를 저장하고 이메일을 발송한다") {
                // given
                val request: SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(email = DummyUser.EMAIL)
                justRun { userBusinessValidator.validate(request = any<SendEmailVerificationCodeRequest>()) }
                justRun { cacheProvider.put(any(), any(), any(), any()) }
                justRun { emailProvider.sendEmail(to = any(), subject = any(), content = any()) }

                // when
                val result: Boolean = service.sendEmailVerificationCode(request)

                // then
                result shouldBe true
                verify(exactly = 1) { cacheProvider.put(eq("${CacheKeyPrefix.EMAIL}${DummyUser.EMAIL}"), any(), any(), any()) }
                verify(exactly = 1) { emailProvider.sendEmail(to = DummyUser.EMAIL, subject = any(), content = any()) }
            }
        }

        `when`("등록되지 않은 이메일이라면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(email = DummyUser.EMAIL)
                every { userBusinessValidator.validate(request = any<SendEmailVerificationCodeRequest>()) } throws InvalidParameterException(
                    errorCode = ErrorCode.USER_EMAIL_NOT_EXISTS
                )

                // when & then
                shouldThrow<InvalidParameterException> {
                    service.sendEmailVerificationCode(request)
                }
            }
        }
    }

    given("이메일 인증 코드 확인 요청이 들어올 때") {
        `when`("인증 코드가 일치하면") {
            then("이메일을 반환하고 Redis에서 인증 코드를 삭제한다") {
                // given
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = DummyUser.EMAIL,
                    verificationCode = DummyAuth.VERIFICATION_CODE
                )
                every { cacheProvider.get("${CacheKeyPrefix.EMAIL}${DummyUser.EMAIL}") } returns DummyAuth.VERIFICATION_CODE
                justRun { cacheProvider.delete("${CacheKeyPrefix.EMAIL}${DummyUser.EMAIL}") }

                // when
                val result: String = service.checkEmailVerificationCode(request)

                // then
                result shouldBe DummyUser.EMAIL
                verify(exactly = 1) { cacheProvider.delete("${CacheKeyPrefix.EMAIL}${DummyUser.EMAIL}") }
            }
        }

        `when`("Redis에 인증 코드가 없으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = DummyUser.EMAIL,
                    verificationCode = DummyAuth.VERIFICATION_CODE
                )
                every { cacheProvider.get("${CacheKeyPrefix.EMAIL}${DummyUser.EMAIL}") } returns null

                // when & then
                shouldThrow<InvalidParameterException> {
                    service.checkEmailVerificationCode(request)
                }
            }
        }

        `when`("인증 코드가 일치하지 않으면") {
            then("InvalidParameterException이 발생한다") {
                // given
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = DummyUser.EMAIL,
                    verificationCode = "wrongCode"
                )
                every { cacheProvider.get("${CacheKeyPrefix.EMAIL}${DummyUser.EMAIL}") } returns DummyAuth.VERIFICATION_CODE

                // when & then
                shouldThrow<InvalidParameterException> {
                    service.checkEmailVerificationCode(request)
                }
            }
        }
    }

    given("로그아웃 요청이 들어올 때") {
        `when`("유효한 refresh token이 주어지면") {
            then("userId 키로 Redis에서 refresh token을 삭제한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                justRun { cacheProvider.delete("${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}") }

                // when
                service.logout(DummyAuth.REFRESH_TOKEN)

                // then
                verify(exactly = 1) { cacheProvider.delete("${CacheKeyPrefix.REFRESH_TOKEN}${DummyUser.ID}") }
            }
        }

        `when`("만료되거나 유효하지 않은 refresh token이 주어지면") {
            then("캐시 삭제 없이 정상 종료한다") {
                // given
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns null

                // when
                service.logout(DummyAuth.REFRESH_TOKEN)

                // then
                verify(exactly = 0) { cacheProvider.delete(any()) }
            }
        }
    }
})
