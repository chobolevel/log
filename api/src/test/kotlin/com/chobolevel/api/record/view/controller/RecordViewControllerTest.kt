package com.chobolevel.api.record.view.controller

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.view.service.RecordViewService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecordViewController::class)
@Import(RecordViewControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("RecordViewController 슬라이스 테스트")
class RecordViewControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var recordViewService: RecordViewService

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
    fun `인증 없이 조회 반영 요청을 할 수 있다`() {
        // given
        every {
            recordViewService.recordView(recordId = DummyRecord.ID, userId = null, guestId = null)
        } returns true

        // when & then
        mockMvc.perform(
            post("/api/v1/records/${DummyRecord.ID}/view")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 조회 반영 요청 시 userId로 반영한다`() {
        // given
        every {
            recordViewService.recordView(recordId = DummyRecord.ID, userId = DummyUser.ID, guestId = null)
        } returns true

        // when & then
        mockMvc.perform(
            post("/api/v1/records/${DummyRecord.ID}/view")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }
}
