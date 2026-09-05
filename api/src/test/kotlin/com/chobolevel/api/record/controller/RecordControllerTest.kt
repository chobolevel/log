package com.chobolevel.api.record.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.service.RecordService
import com.chobolevel.api.record.validator.RecordParameterValidator
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

@WebMvcTest(RecordController::class)
@Import(RecordControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("RecordController 슬라이스 테스트")
class RecordControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var recordService: RecordService

    @MockkBean
    private lateinit var recordParameterValidator: RecordParameterValidator

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
    fun `인증된 사용자가 기록 등록 요청 시 기록 등록 후 ID를 반환한다`() {
        // given
        every { recordService.createRecord(userId = any(), request = any()) } returns DummyRecord.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyRecord.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyRecord.ID))
    }

    @Test
    fun `인증 없이 기록 등록 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyRecord.toCreateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `인증 없이 기록 목록을 조회할 수 있다`() {
        // given
        every {
            recordService.searchRecords(requesterId = null, filter = any(), pageRequest = any())
        } returns PagingResponse(
            page = 1L,
            size = 20L,
            data = listOf<RecordResponse>(DummyRecord.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(get("/api/v1/records"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.data").isArray)
            .andExpect(jsonPath("$.data.total_count").value(1L))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 기록 목록을 조회할 수 있다`() {
        // given
        every {
            recordService.searchRecords(requesterId = DummyUser.ID, filter = any(), pageRequest = any())
        } returns PagingResponse(
            page = 1L,
            size = 20L,
            data = listOf<RecordResponse>(DummyRecord.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(get("/api/v1/records"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    fun `인증 없이 기록 단건을 조회할 수 있다`() {
        // given
        every {
            recordService.fetchRecord(requesterId = null, recordId = DummyRecord.ID)
        } returns DummyRecord.toResponse()

        // when & then
        mockMvc.perform(get("/api/v1/records/${DummyRecord.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyRecord.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 기록 수정 요청 시 기록 수정 후 기록 id를 반환한다`() {
        // given
        justRun { recordParameterValidator.validate(request = any()) }
        every {
            recordService.updateRecord(userId = DummyUser.ID, recordId = DummyRecord.ID, request = any())
        } returns DummyRecord.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/records/${DummyRecord.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyRecord.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyRecord.ID))
    }

    @Test
    fun `인증 없이 기록 수정 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            put("/api/v1/records/${DummyRecord.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyRecord.toUpdateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 기록 삭제 요청 시 기록 삭제 후 true를 반환한다`() {
        // given
        every {
            recordService.deleteRecord(userId = DummyUser.ID, recordId = DummyRecord.ID)
        } returns true

        // when & then
        mockMvc.perform(delete("/api/v1/records/${DummyRecord.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `인증 없이 기록 삭제 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(delete("/api/v1/records/${DummyRecord.ID}"))
            .andExpect(status().isUnauthorized)
    }
}
