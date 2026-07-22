package com.chobolevel.api.user.controller

import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.user.service.UserService
import com.chobolevel.api.user.validator.UserParameterValidator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.AnnotationSpec.BeforeEach
import io.kotest.core.spec.style.Test
import io.mockk.clearAllMocks
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
@Import(UserControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userParameterValidator: UserParameterValidator

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain =
            http
                .csrf { it.disable() } // 비활성화하지 않으면 POST 요청이 CSRF 토큰 없이 403
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { it.anyRequest().permitAll() } // URL 레벨은 전부 허용, 인가는 @PreAuthorize가 담당
                .build()
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `회원가입 요청 시 회원 등록 후 회원 ID를 반환하다`() {
        // given
        every { userService.createUser(request = any()) } returns DummyUser.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyUser.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(DummyUser.ID))
    }
}
