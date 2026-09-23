package com.chobolevel.api.record.view.sync.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyRecordViewSyncEvent
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.view.sync.dto.RecordViewSyncEventResponse
import com.chobolevel.api.record.view.sync.service.RecordViewSyncEventService
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

@WebMvcTest(RecordViewSyncEventController::class)
@Import(RecordViewSyncEventControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("RecordViewSyncEventController 슬라이스 테스트")
class RecordViewSyncEventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var recordViewSyncEventService: RecordViewSyncEventService

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
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 실패한 이벤트 목록 조회 요청 시 목록을 반환한다`() {
        // given
        every {
            recordViewSyncEventService.searchFailedEvents(request = any())
        } returns PagingResponse(
            page = 1L,
            size = 100L,
            data = listOf<RecordViewSyncEventResponse>(DummyRecordViewSyncEvent.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(
            get("/api/v1/record-view-sync-events/failed")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.data").isArray)
            .andExpect(jsonPath("$.data.total_count").value(1L))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 실패한 이벤트 목록 조회 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            get("/api/v1/record-view-sync-events/failed")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 이벤트 재발행 요청 시 이벤트 id를 반환한다`() {
        // given
        every {
            recordViewSyncEventService.retry(eventId = DummyRecordViewSyncEvent.ID)
        } returns DummyRecordViewSyncEvent.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/record-view-sync-events/${DummyRecordViewSyncEvent.ID}/retry")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyRecordViewSyncEvent.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 이벤트 재발행 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            post("/api/v1/record-view-sync-events/${DummyRecordViewSyncEvent.ID}/retry")
        )
            .andExpect(status().isUnauthorized)
    }
}
