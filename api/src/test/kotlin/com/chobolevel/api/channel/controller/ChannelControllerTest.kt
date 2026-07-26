package com.chobolevel.api.channel.controller

import com.chobolevel.api.channel.dto.UpdateChannelRequest
import com.chobolevel.api.channel.service.ChannelService
import com.chobolevel.api.channel.validator.ChannelParameterValidator
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyChannel
import com.chobolevel.api.common.dummy.DummyUser
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

@WebMvcTest(ChannelController::class)
@Import(ChannelControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("ChannelController 슬라이스 테스트")
class ChannelControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var channelService: ChannelService

    @MockkBean
    private lateinit var channelParameterValidator: ChannelParameterValidator

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
    fun `인증된 사용자가 채널 생성 요청 시 채널 id를 반환한다`() {
        // given
        every { channelService.create(ownerId = DummyUser.ID, request = any()) } returns DummyChannel.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/channels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyChannel.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyChannel.ID))
    }

    @Test
    fun `미인증 사용자가 채널 생성 시도하면 401을 반환한다`() {
        mockMvc.perform(
            post("/api/v1/channels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyChannel.toCreateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `채널 이름이 비어있는 채널 생성 요청은 400을 반환한다`() {
        // given — @Valid + @NotEmpty 검증 실패
        val invalidRequest = DummyChannel.toCreateRequest().copy(name = "")

        // when & then
        mockMvc.perform(
            post("/api/v1/channels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("채널 이름은 필수입니다."))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널 목록을 조회할 수 있다`() {
        // given
        every { channelService.getChannels(userId = DummyUser.ID, pageRequest = any()) } returns
            PagingResponse(
                page = 1L,
                size = 50L,
                data = listOf(DummyChannel.toResponse()),
                totalCount = 1L
            )

        // when & then
        mockMvc.perform(get("/api/v1/channels"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.total_count").value(1L))
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    fun `미인증 사용자가 채널 목록 조회 시도하면 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/channels"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널 단건을 조회할 수 있다`() {
        // given
        every { channelService.getChannel(userId = DummyUser.ID, channelId = DummyChannel.ID) } returns DummyChannel.toResponse()

        // when & then
        mockMvc.perform(get("/api/v1/channels/${DummyChannel.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyChannel.ID))
            .andExpect(jsonPath("$.data.name").value(DummyChannel.NAME))
    }

    @Test
    fun `미인증 사용자가 채널 단건 조회 시도하면 401을 반환한다`() {
        mockMvc.perform(get("/api/v1/channels/${DummyChannel.ID}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널 정보를 수정할 수 있다`() {
        // given
        justRun { channelParameterValidator.validate(request = any<UpdateChannelRequest>()) }
        every {
            channelService.update(workerId = DummyUser.ID, channelId = DummyChannel.ID, request = DummyChannel.toUpdateRequest())
        } returns DummyChannel.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/channels/${DummyChannel.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyChannel.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyChannel.ID))
    }

    @Test
    fun `미인증 사용자가 채널 수정 시도하면 401을 반환한다`() {
        mockMvc.perform(
            put("/api/v1/channels/${DummyChannel.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyChannel.toUpdateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널을 떠날 수 있다`() {
        // given
        every { channelService.exit(userId = DummyUser.ID, channelId = DummyChannel.ID) } returns DummyChannel.ID

        // when & then
        mockMvc.perform(put("/api/v1/channels/${DummyChannel.ID}/exit"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyChannel.ID))
    }

    @Test
    fun `미인증 사용자가 채널 떠나기 시도하면 401을 반환한다`() {
        mockMvc.perform(put("/api/v1/channels/${DummyChannel.ID}/exit"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널에 유저를 초대할 수 있다`() {
        // given
        every {
            channelService.invite(userId = DummyUser.ID, channelId = DummyChannel.ID, request = DummyChannel.toInviteRequest())
        } returns DummyChannel.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/channels/${DummyChannel.ID}/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyChannel.toInviteRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyChannel.ID))
    }

    @Test
    fun `미인증 사용자가 채널 초대 시도하면 401을 반환한다`() {
        mockMvc.perform(
            put("/api/v1/channels/${DummyChannel.ID}/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyChannel.toInviteRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}")
    fun `인증된 사용자가 채널을 삭제할 수 있다`() {
        // given
        every { channelService.delete(workerId = DummyUser.ID, channelId = DummyChannel.ID) } returns true

        // when & then
        mockMvc.perform(delete("/api/v1/channels/${DummyChannel.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `미인증 사용자가 채널 삭제 시도하면 401을 반환한다`() {
        mockMvc.perform(delete("/api/v1/channels/${DummyChannel.ID}"))
            .andExpect(status().isUnauthorized)
    }
}
