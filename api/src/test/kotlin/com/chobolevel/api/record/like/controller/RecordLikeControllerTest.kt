package com.chobolevel.api.record.like.controller

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.like.service.RecordLikeService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecordLikeController::class)
@Import(RecordLikeControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("RecordLikeController 슬라이스 테스트")
class RecordLikeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var recordLikeService: RecordLikeService

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
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 좋아요 요청 시 좋아요 수를 반환한다`() {
        // given
        every { recordLikeService.like(userId = DummyUser.ID, recordId = DummyRecord.ID) } returns 1L

        // when & then
        mockMvc.perform(post("/api/v1/records/${DummyRecord.ID}/like"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(1L))
    }

    @Test
    fun `인증 없이 좋아요 요청 시 401을 반환한다`() {
        mockMvc.perform(post("/api/v1/records/${DummyRecord.ID}/like"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 좋아요 취소 요청 시 좋아요 수를 반환한다`() {
        // given
        every { recordLikeService.dislike(userId = DummyUser.ID, recordId = DummyRecord.ID) } returns 0L

        // when & then
        mockMvc.perform(post("/api/v1/records/${DummyRecord.ID}/dislike"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(0L))
    }

    @Test
    fun `인증 없이 좋아요 취소 요청 시 401을 반환한다`() {
        mockMvc.perform(post("/api/v1/records/${DummyRecord.ID}/dislike"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `인증 없이 좋아요 수를 조회할 수 있다`() {
        // given
        every { recordLikeService.fetchLikeCount(recordId = DummyRecord.ID) } returns 5L

        // when & then
        mockMvc.perform(get("/api/v1/records/${DummyRecord.ID}/likes/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(5L))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 좋아요 여부 조회 시 결과를 반환한다`() {
        // given
        every { recordLikeService.isLiked(userId = DummyUser.ID, recordId = DummyRecord.ID) } returns true

        // when & then
        mockMvc.perform(get("/api/v1/records/${DummyRecord.ID}/likes/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `인증 없이 좋아요 여부 조회 시 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/records/${DummyRecord.ID}/likes/me"))
            .andExpect(status().isUnauthorized)
    }
}
