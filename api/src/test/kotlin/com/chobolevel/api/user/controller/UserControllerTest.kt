package com.chobolevel.api.user.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.api.user.service.UserService
import com.chobolevel.api.user.validator.UserParameterValidator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
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
        justRun { userParameterValidator.validate(request = any<CreateUserRequest>()) }
        every { userService.createUser(request = any()) } returns DummyUser.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyUser.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyUser.ID))
    }

    @Test
    fun `인증 없이 회원 목록을 조회할 수 있다`() {
        // given
        every {
            userService.searchUsers(
                filter = any(),
                pageRequest = any()
            )
        } returns PagingResponse(
            page = 1L,
            size = 20L,
            data = listOf(DummyUser.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.total_count").value(1L))
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    fun `인증 없이 회원 단건 조회할 수 있다`() {
        // given
        every {
            userService.fetchUser(
                id = any()
            )
        } returns DummyUser.toResponse()

        // when & then
        mockMvc.perform(get("/api/v1/users/${DummyUser.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyUser.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 본인 회원 정보를 조회할 수 있다`() {
        // given
        every {
            userService.fetchUser(
                id = DummyUser.ID
            )
        } returns DummyUser.toResponse()

        // when & then
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyUser.ID))
    }

    @Test
    fun `미인증 사용자가 본인 회원 정보 조회 시독하면 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 본인 회원 정보를 수정할 수 있다`() {
        // given
        justRun { userParameterValidator.validate(request = any<UpdateUserRequest>()) }
        every {
            userService.updateUser(
                id = DummyUser.ID,
                request = DummyUser.toUpdateRequest()
            )
        } returns DummyUser.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyUser.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyUser.ID))
    }

    @Test
    fun `미인증 사용자가 본인 회원 정보 수정 시도하면 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyUser.toUpdateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 본인 회원 비밀번호를 변경할 수 있다`() {
        // given
        justRun { userParameterValidator.validate(request = any<ChangeUserPasswordRequest>()) }
        every {
            userService.changePassword(
                id = DummyUser.ID,
                request = DummyUser.toChangePasswordRequest()
            )
        } returns DummyUser.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyUser.toChangePasswordRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyUser.ID))
    }

    @Test
    fun `미인증 사용자가 본인 회원 비밀번호 변경 시도하면 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            put("/api/v1/users/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyUser.toChangePasswordRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 본인 회원 탈퇴할 수 있다`() {
        // given
        every {
            userService.resignUser(
                id = DummyUser.ID
            )
        } returns true

        // when & then
        mockMvc.perform(delete("/api/v1/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `미인증 사용자가 본인 회원 탈퇴 시도하면 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            delete("/api/v1/users/me")
        )
            .andExpect(status().isUnauthorized)
    }
}
