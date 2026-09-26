package com.chobolevel.api.record.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.FetchRecordContributionsRequest
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.service.RecordService
import com.chobolevel.api.record.validator.RecordParameterValidator
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
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
import java.time.LocalDate
import java.time.ZoneId

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
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `기록 등록 요청 시 중첩된 emotion의 intensity가 범위를 벗어나면 400을 반환한다`() {
        // given
        val request: CreateRecordRequest = DummyRecord.toCreateRequest().let {
            it.copy(emotion = it.emotion!!.copy(intensity = 999))
        }

        // when & then
        mockMvc.perform(
            post("/api/v1/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
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
    fun `기록 목록을 조회할 수 있다`() {
        // given
        every {
            recordService.searchRecords(request = any())
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
    fun `인증 없이 기록 단건을 조회할 수 있다`() {
        // given
        every {
            recordService.fetchRecord(requesterId = null, recordId = DummyRecord.ID)
        } returns DummyRecord.toDetailResponse()

        // when & then
        mockMvc.perform(get("/api/v1/records/${DummyRecord.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyRecord.ID))
    }

    @Test
    fun `인증 없이 기록 잔디를 조회할 수 있다`() {
        // given
        justRun { recordParameterValidator.validate(request = any<FetchRecordContributionsRequest>()) }
        every {
            recordService.fetchContributions(userId = DummyUser.ID, year = 2026)
        } returns listOf(DummyRecord.toContributionResponse())

        // when & then
        mockMvc.perform(get("/api/v1/records/contributions?user_id=${DummyUser.ID}&year=2026"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `기록 잔디 조회 시 year를 생략하면 현재 연도로 조회한다`() {
        // given
        val currentYear: Int = LocalDate.now(ZoneId.of("Asia/Seoul")).year
        justRun { recordParameterValidator.validate(request = any<FetchRecordContributionsRequest>()) }
        every {
            recordService.fetchContributions(userId = DummyUser.ID, year = currentYear)
        } returns listOf(DummyRecord.toContributionResponse())

        // when & then
        mockMvc.perform(get("/api/v1/records/contributions?user_id=${DummyUser.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
        verify { recordService.fetchContributions(userId = DummyUser.ID, year = currentYear) }
    }

    @Test
    fun `기록 잔디 조회 시 year가 숫자 형식이 아니면 400을 반환한다`() {
        // given & when & then
        mockMvc.perform(get("/api/v1/records/contributions?user_id=${DummyUser.ID}&year=abc"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(ErrorCode.INVALID_PARAMETER.name))
    }

    @Test
    fun `기록 잔디 조회 시 user_id가 없으면 400을 반환한다`() {
        // given
        every {
            recordParameterValidator.validate(request = any<FetchRecordContributionsRequest>())
        } throws InvalidParameterException(errorCode = ErrorCode.INVALID_PARAMETER, message = "user_id는 필수 값입니다.")

        // when & then
        mockMvc.perform(get("/api/v1/records/contributions?year=2026"))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 기록 수정 요청 시 기록 수정 후 기록 id를 반환한다`() {
        // given
        justRun { recordParameterValidator.validate(request = any<UpdateRecordRequest>()) }
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
