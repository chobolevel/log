package com.chobolevel.api.channel.message.controller

import com.chobolevel.api.channel.message.service.ChannelMessageService
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyChannel
import com.chobolevel.api.common.dummy.DummyChannelMessage
import com.chobolevel.api.common.dummy.DummyUser
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ChannelMessageController::class)
@Import(ChannelMessageControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("ChannelMessageController 슬라이스 테스트")
class ChannelMessageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var channelMessageService: ChannelMessageService

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
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널 메세지 목록을 조회할 수 있다`() {
        // given
        every {
            channelMessageService.getChannelMessages(channelId = DummyChannel.ID, pageRequest = any())
        } returns PagingResponse(
            page = 1L,
            size = 50L,
            data = listOf(DummyChannelMessage.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(get("/api/v1/channels/${DummyChannel.ID}/messages"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.total_count").value(1L))
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    fun `미인증 사용자가 채널 메세지 목록 조회 시도하면 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/channels/${DummyChannel.ID}/messages"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널 메세지를 삭제할 수 있다`() {
        // given
        every {
            channelMessageService.delete(workerId = DummyUser.ID, channelMessageId = DummyChannelMessage.ID)
        } returns true

        // when & then
        mockMvc.perform(delete("/api/v1/channels/${DummyChannel.ID}/messages/${DummyChannelMessage.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `미인증 사용자가 채널 메세지 삭제 시도하면 401을 반환한다`() {
        mockMvc.perform(delete("/api/v1/channels/${DummyChannel.ID}/messages/${DummyChannelMessage.ID}"))
            .andExpect(status().isUnauthorized)
    }
}
