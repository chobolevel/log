package com.chobolevel.api.auth.service

import com.chobolevel.api.auth.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.auth.dto.JwtResponse
import com.chobolevel.api.auth.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.common.dummy.DummyAuth
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.security.CustomAuthenticationManager
import com.chobolevel.api.common.security.TokenProvider
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.utils.EmailUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.redis.core.HashOperations
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

class AuthServiceTest : BehaviorSpec({

    val tokenProvider: TokenProvider = mockk()
    val authenticationManager: CustomAuthenticationManager = mockk()
    val opsForHash: HashOperations<String, String, String> = mockk()
    val emailUtils: EmailUtils = mockk()
    val service: AuthService = AuthService(
        tokenProvider = tokenProvider,
        authenticationManager = authenticationManager,
        opsForHash = opsForHash,
        emailUtils = emailUtils
    )

    beforeEach {
        clearAllMocks()
    }

    given("일반 로그인 요청이 들어올 때") {
        `when`("이메일과 비밀번호가 유효하면") {
            then("JWT 토큰을 반환하고 Redis에 refresh token을 저장한다") {
                // given
                val request = DummyAuth.toGeneralLoginRequest()
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                val authSlot: CapturingSlot<Authentication> = slot()
                // slot으로 캡처해서 "email/loginType" 조합 포맷이 실제로 지켜지는지 검증한다.
                every { authenticationManager.authenticate(capture(authSlot)) } returns authentication
                every { tokenProvider.generateToken(authentication) } returns jwtResponse
                every { opsForHash.put(any(), any(), any()) } returns Unit

                // when
                val result: JwtResponse = service.login(request)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
                authSlot.captured.principal shouldBe "${DummyUser.EMAIL}/GENERAL"
                verify(exactly = 1) { opsForHash.put("refresh-token:v1", DummyAuth.REFRESH_TOKEN, DummyUser.ID.toString()) }
            }
        }
    }

    given("카카오 소셜 로그인 요청이 들어올 때") {
        `when`("이메일과 소셜 id가 유효하면") {
            then("JWT 토큰을 반환하고 Redis에 refresh token을 저장한다") {
                // given
                val request = DummyAuth.toKakaoLoginRequest()
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                val authSlot: CapturingSlot<Authentication> = slot()
                every { authenticationManager.authenticate(capture(authSlot)) } returns authentication
                every { tokenProvider.generateToken(authentication) } returns jwtResponse
                every { opsForHash.put(any(), any(), any()) } returns Unit

                // when
                val result: JwtResponse = service.login(request)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
                authSlot.captured.principal shouldBe "${DummyUser.EMAIL}/KAKAO"
                verify(exactly = 1) { opsForHash.put("refresh-token:v1", DummyAuth.REFRESH_TOKEN, DummyUser.ID.toString()) }
            }
        }
    }

    given("토큰 갱신 요청이 들어올 때") {
        `when`("유효한 refresh token이고 Redis에 userId가 일치하면") {
            then("새 JWT 토큰을 반환한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                val jwtResponse: JwtResponse = DummyAuth.toJwtResponse()
                every { tokenProvider.validateToken(DummyAuth.REFRESH_TOKEN) } returns true
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                every { opsForHash.get("refresh-token:v1", DummyAuth.REFRESH_TOKEN) } returns DummyUser.ID.toString()
                every { tokenProvider.generateToken(authentication) } returns jwtResponse

                // when
                val result: JwtResponse = service.reissue(DummyAuth.REFRESH_TOKEN)

                // then
                result.accessToken shouldBe DummyAuth.ACCESS_TOKEN
                result.refreshToken shouldBe DummyAuth.REFRESH_TOKEN
            }
        }

        `when`("Redis에 저장된 refresh token이 없으면") {
            then("ApiException이 발생한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                every { tokenProvider.validateToken(DummyAuth.REFRESH_TOKEN) } returns true
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                every { opsForHash.get("refresh-token:v1", DummyAuth.REFRESH_TOKEN) } returns null

                // when & then
                shouldThrow<ApiException> {
                    service.reissue(DummyAuth.REFRESH_TOKEN)
                }
            }
        }

        `when`("Redis의 userId와 토큰의 userId가 다르면") {
            then("ApiException이 발생한다") {
                // given
                val authentication = UsernamePasswordAuthenticationToken(DummyUser.ID.toString(), null)
                every { tokenProvider.validateToken(DummyAuth.REFRESH_TOKEN) } returns true
                every { tokenProvider.getAuthentication(DummyAuth.REFRESH_TOKEN) } returns authentication
                every { opsForHash.get("refresh-token:v1", DummyAuth.REFRESH_TOKEN) } returns "999"

                // when & then
                shouldThrow<ApiException> {
                    service.reissue(DummyAuth.REFRESH_TOKEN)
                }
            }
        }
    }

    given("이메일 인증 코드 발송 요청이 들어올 때") {
        `when`("이메일 주소가 주어지면") {
            then("Redis에 인증 코드를 저장하고 이메일을 발송한다") {
                // given
                val request: SendEmailVerificationCodeRequest = SendEmailVerificationCodeRequest(email = DummyUser.EMAIL)
                every { opsForHash.put(eq("email"), eq(DummyUser.EMAIL), any()) } returns Unit
                justRun { emailUtils.sendEmail(email = any(), subject = any(), content = any()) }

                // when
                service.asyncSendEmailVerificationCode(request)

                // then
                verify(exactly = 1) { opsForHash.put(eq("email"), eq(DummyUser.EMAIL), any()) }
                verify(exactly = 1) { emailUtils.sendEmail(email = DummyUser.EMAIL, subject = any(), content = any()) }
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
                every { opsForHash.get("email", DummyUser.EMAIL) } returns DummyAuth.VERIFICATION_CODE
                every { opsForHash.delete("email", DummyUser.EMAIL) } returns 1L

                // when
                val result: String = service.checkEmailVerificationCode(request)

                // then
                result shouldBe DummyUser.EMAIL
                verify(exactly = 1) { opsForHash.delete("email", DummyUser.EMAIL) }
            }
        }

        `when`("Redis에 인증 코드가 없으면") {
            then("ApiException이 발생한다") {
                // given
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = DummyUser.EMAIL,
                    verificationCode = DummyAuth.VERIFICATION_CODE
                )
                every { opsForHash.get("email", DummyUser.EMAIL) } returns null

                // when & then
                shouldThrow<ApiException> {
                    service.checkEmailVerificationCode(request)
                }
            }
        }

        `when`("인증 코드가 일치하지 않으면") {
            then("ApiException이 발생한다") {
                // given
                val request: CheckEmailVerificationCodeRequest = CheckEmailVerificationCodeRequest(
                    email = DummyUser.EMAIL,
                    verificationCode = "wrongCode"
                )
                every { opsForHash.get("email", DummyUser.EMAIL) } returns DummyAuth.VERIFICATION_CODE

                // when & then
                shouldThrow<ApiException> {
                    service.checkEmailVerificationCode(request)
                }
            }
        }
    }

    given("로그아웃 요청이 들어올 때") {
        `when`("유효한 refresh token이 주어지면") {
            then("Redis에서 refresh token을 삭제한다") {
                // given
                every { opsForHash.delete("refresh-token:v1", DummyAuth.REFRESH_TOKEN) } returns 1L

                // when
                service.logout(DummyAuth.REFRESH_TOKEN)

                // then
                verify(exactly = 1) { opsForHash.delete("refresh-token:v1", DummyAuth.REFRESH_TOKEN) }
            }
        }
    }
})
