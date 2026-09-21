package com.chobolevel.api.user.follow.controller

import com.chobolevel.api.user.follow.dto.UserFollowCounterResponse
import com.chobolevel.api.user.follow.service.UserFollowFacade
import com.chobolevel.api.user.follow.service.UserFollowQueryService
import com.chobolevel.api.user.follow.service.UserFollowService
import com.chobolevel.api.user.follow.validator.UserFollowParameterValidator
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserFollowController::class)
@Import(UserFollowControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("UserFollowController 슬라이스 테스트")
class UserFollowControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var facade: UserFollowFacade

    @MockkBean
    private lateinit var service: UserFollowService

    @MockkBean
    private lateinit var queryService: UserFollowQueryService

    @MockkBean
    private lateinit var validator: UserFollowParameterValidator

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
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
    }

    @Test
    // @WithMockUser: username="1" → getUserId() = 1L, 기본 role은 USER
    @WithMockUser(username = "1")
    fun `인증된 사용자가 팔로우를 요청하면 Facade에 위임하고 결과를 반환한다`() {
        // given
        justRun { validator.validateFollow(followerUserId = 1L, followingUserId = 2L) }
        every { facade.follow(followerUserId = 1L, followingUserId = 2L) } returns true

        // when & then
        mockMvc.perform(post("/api/v1/users/2/follow"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `미인증 사용자가 팔로우를 시도하면 401을 반환한다`() {
        mockMvc.perform(post("/api/v1/users/2/follow"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1")
    fun `인증된 사용자가 언팔로우를 요청하면 Facade에 위임하고 결과를 반환한다`() {
        // given
        every { facade.unfollow(followerUserId = 1L, followingUserId = 2L) } returns true

        // when & then
        mockMvc.perform(post("/api/v1/users/2/unfollow"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    @WithMockUser(username = "1", roles = ["ADMIN"])
    fun `관리자가 카운터 재계산을 요청하면 재계산된 카운터를 반환한다`() {
        // given
        every { service.recalculateFollowCounters(userId = 2L) } returns UserFollowCounterResponse(
            followerCount = 20L,
            followingCount = 10L,
        )

        // when & then
        mockMvc.perform(post("/api/v1/users/2/follow-counters/recalculate"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.follower_count").value(20L))
            .andExpect(jsonPath("$.data.following_count").value(10L))
    }

    @Test
    @WithMockUser(username = "1")
    fun `관리자가 아닌 사용자가 카운터 재계산을 시도하면 401을 반환한다`() {
        mockMvc.perform(post("/api/v1/users/2/follow-counters/recalculate"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `미인증 사용자가 카운터 재계산을 시도하면 401을 반환한다`() {
        mockMvc.perform(post("/api/v1/users/2/follow-counters/recalculate"))
            .andExpect(status().isUnauthorized)
    }
}
