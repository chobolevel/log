package com.chobolevel.api.user.controller

import com.chobolevel.api.common.dummy.DummyAuth
import com.chobolevel.api.common.properties.JwtProperties
import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.LoginRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.SocialLoginRequest
import com.chobolevel.api.user.service.UserAuthService
import com.chobolevel.api.user.validator.UserAuthParameterValidator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserAuthController::class)
@Import(UserAuthControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("UserAuthController 슬라이스 테스트")
class UserAuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var userAuthService: UserAuthService

    @MockkBean
    private lateinit var jwtProperties: JwtProperties

    @MockkBean
    private lateinit var userAuthParameterValidator: UserAuthParameterValidator

    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain =
            http
                .csrf { it.disable() }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { it.anyRequest().permitAll() }
                .build()
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { jwtProperties.accessTokenKey } returns DummyAuth.ACCESS_TOKEN_COOKIE_KEY
        every { jwtProperties.refreshTokenKey } returns DummyAuth.REFRESH_TOKEN_COOKIE_KEY
        every { jwtProperties.cookie } returns JwtProperties.Cookie(
            path = "/",
            maxAge = 3600,
            domain = "localhost",
            secure = false,
            httpOnly = true,
            sameSite = "none"
        )
    }

    @Test
    fun `유효한 일반 로그인 요청 시 200을 반환하고 응답 쿠키에 토큰이 담긴다`() {
        // given
        justRun { userAuthParameterValidator.validate(request = any<LoginRequest>()) }
        every { userAuthService.login(request = any()) } returns DummyAuth.toJwtResponse()

        // when & then
        mockMvc.perform(
            post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyAuth.toGeneralLoginRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
            .andExpect(cookie().value(DummyAuth.ACCESS_TOKEN_COOKIE_KEY, DummyAuth.ACCESS_TOKEN))
            .andExpect(cookie().value(DummyAuth.REFRESH_TOKEN_COOKIE_KEY, DummyAuth.REFRESH_TOKEN))
    }

    @Test
    fun `이메일이 비어있는 로그인 요청은 400을 반환한다`() {
        // given — @Valid가 email의 @NotEmpty를 검증하므로 서비스 호출 없이 실패
        val invalidRequest: LoginRequest = DummyAuth.toGeneralLoginRequest().copy(email = "")

        // when & then
        mockMvc.perform(
            post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("아이디(이메일)는 필수 값입니다."))
    }

    @Test
    fun `비밀번호가 비어있는 로그인 요청은 400을 반환한다`() {
        // given — @Valid가 password의 @NotEmpty를 검증하므로 서비스 호출 없이 실패
        val invalidRequest: LoginRequest = DummyAuth.toGeneralLoginRequest().copy(password = "")

        // when & then
        mockMvc.perform(
            post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("비밀번호는 필수 값입니다."))
    }

    @Test
    fun `유효한 소셜 로그인 요청 시 200을 반환하고 응답 쿠키에 토큰이 담긴다`() {
        // given
        justRun { userAuthParameterValidator.validate(request = any<SocialLoginRequest>()) }
        every { userAuthService.socialLogin(request = any()) } returns DummyAuth.toJwtResponse()

        // when & then
        mockMvc.perform(
            post("/api/v1/users/social-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyAuth.toGithubSocialLoginRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
            .andExpect(cookie().value(DummyAuth.ACCESS_TOKEN_COOKIE_KEY, DummyAuth.ACCESS_TOKEN))
            .andExpect(cookie().value(DummyAuth.REFRESH_TOKEN_COOKIE_KEY, DummyAuth.REFRESH_TOKEN))
    }

    @Test
    fun `refresh 토큰 쿠키가 있을 때 로그아웃하면 200을 반환하고 토큰 쿠키가 만료된다`() {
        // given
        justRun { userAuthService.logout(refreshToken = any()) }

        // when & then
        mockMvc.perform(
            post("/api/v1/users/logout")
                .cookie(Cookie(DummyAuth.REFRESH_TOKEN_COOKIE_KEY, DummyAuth.REFRESH_TOKEN))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
            .andExpect(cookie().maxAge("_cat", 0))
            .andExpect(cookie().maxAge("_crt", 0))
    }

    @Test
    fun `refresh 토큰 쿠키가 없어도 로그아웃은 200을 반환한다`() {
        // given — 쿠키 없으면 service.logout 자체를 호출하지 않는다 (controller 분기)

        // when & then
        mockMvc.perform(post("/api/v1/users/logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `유효한 refresh 토큰 쿠키로 재발급 요청 시 200을 반환하고 새 access 토큰 쿠키가 담긴다`() {
        // given
        every { userAuthService.reissue(refreshToken = DummyAuth.REFRESH_TOKEN) } returns DummyAuth.toJwtResponse()

        // when & then
        mockMvc.perform(
            post("/api/v1/users/reissue")
                .cookie(Cookie(DummyAuth.REFRESH_TOKEN_COOKIE_KEY, DummyAuth.REFRESH_TOKEN))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
            .andExpect(cookie().value(DummyAuth.ACCESS_TOKEN_COOKIE_KEY, DummyAuth.ACCESS_TOKEN))
    }

    @Test
    fun `refresh 토큰 쿠키 없이 재발급 요청 시 401을 반환한다`() {
        // given — 쿠키 없음 → controller에서 ApiException(INVALID_TOKEN) 발생 → 401

        // when & then
        mockMvc.perform(post("/api/v1/users/reissue"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `이메일 인증 코드 전송 요청 시 200을 반환한다`() {
        // given
        justRun { userAuthParameterValidator.validate(request = any<SendEmailVerificationCodeRequest>()) }
        every { userAuthService.sendEmailVerificationCode(request = any()) } returns true

        // when & then
        mockMvc.perform(
            post("/api/v1/users/email-verifications/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyAuth.toSendEmailVerificationCodeRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `이메일 인증 코드 확인 요청 시 200과 토큰을 반환한다`() {
        // given
        justRun { userAuthParameterValidator.validate(request = any<CheckEmailVerificationCodeRequest>()) }
        every { userAuthService.checkEmailVerificationCode(request = any()) } returns DummyAuth.ACCESS_TOKEN

        // when & then
        mockMvc.perform(
            post("/api/v1/users/email-verifications/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyAuth.toCheckEmailVerificationCodeRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyAuth.ACCESS_TOKEN))
    }
}
