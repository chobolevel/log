package com.chobolevel.api.record.feed.controller

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.web.CursorPagingResponse
import com.chobolevel.api.record.controller.RecordController
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.api.record.feed.service.RecordFeedService
import com.chobolevel.api.record.feed.validator.RecordFeedParameterValidator
import com.chobolevel.api.record.service.RecordService
import com.chobolevel.api.record.validator.RecordParameterValidator
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.record.exception.RecordErrorCode
import com.ninjasquad.springmockk.MockkBean
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// /records/feed가 /records/{id}와 경로가 겹치므로 두 컨트롤러를 함께 올려 라우팅까지 고정한다.
@WebMvcTest(controllers = [RecordFeedController::class, RecordController::class])
@Import(RecordFeedControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("RecordFeedController 슬라이스 테스트")
class RecordFeedControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var recordFeedService: RecordFeedService

    @MockkBean
    private lateinit var recordFeedParameterValidator: RecordFeedParameterValidator

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
        justRun { recordFeedParameterValidator.validate(any<SearchRecordFeedRequest>()) }
    }

    private fun feedPage(nextCursor: String?): CursorPagingResponse<RecordResponse> {
        return CursorPagingResponse(data = listOf(DummyRecord.toResponse()), nextCursor = nextCursor)
    }

    @Test
    fun `인증 없이 기록 피드를 조회할 수 있다`() {
        // given
        every {
            recordFeedService.searchRecordFeeds(requesterId = null, request = any())
        } returns feedPage(nextCursor = "bmV4dA")

        // when & then
        mockMvc.perform(get("/api/v1/records/feed"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.data").isArray)
            .andExpect(jsonPath("$.data.next_cursor").value("bmV4dA"))

        verify { recordFeedService.searchRecordFeeds(requesterId = null, request = any()) }
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 요청하면 본인 id가 서비스에 전달된다`() {
        // given
        every {
            recordFeedService.searchRecordFeeds(requesterId = DummyUser.ID, request = any())
        } returns feedPage(nextCursor = null)

        // when & then
        mockMvc.perform(get("/api/v1/records/feed"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.next_cursor").doesNotExist())

        verify { recordFeedService.searchRecordFeeds(requesterId = DummyUser.ID, request = any()) }
    }

    @Test
    fun `cursor와 size 쿼리 파라미터가 요청 객체로 전달되고 검증된다`() {
        // given
        val requestSlot: CapturingSlot<SearchRecordFeedRequest> = slot()
        every {
            recordFeedService.searchRecordFeeds(requesterId = any(), request = capture(requestSlot))
        } returns feedPage(nextCursor = null)

        // when
        mockMvc.perform(get("/api/v1/records/feed").param("cursor", "MTAw").param("size", "5"))
            .andExpect(status().isOk)

        // then
        assert(requestSlot.captured.cursor == "MTAw")
        assert(requestSlot.captured.size == 5L)
        verify { recordFeedParameterValidator.validate(requestSlot.captured) }
    }

    @Test
    fun `cursor 검증에 실패하면 400을 반환한다`() {
        // given
        every {
            recordFeedParameterValidator.validate(any<SearchRecordFeedRequest>())
        } throws BusinessException(errorCode = RecordErrorCode.INVALID_FEED_CURSOR)

        // when & then
        mockMvc.perform(get("/api/v1/records/feed").param("cursor", "broken"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value("INVALID_FEED_CURSOR"))
    }

    @Test
    fun `size 검증에 실패하면 400을 반환한다`() {
        // given
        every {
            recordFeedParameterValidator.validate(any<SearchRecordFeedRequest>())
        } throws BusinessException(errorCode = CommonErrorCode.INVALID_PARAMETER)

        // when & then
        mockMvc.perform(get("/api/v1/records/feed").param("size", "51"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `feed 경로는 기록 단건 조회로 라우팅되지 않고 숫자 id 경로는 기존대로 단건 조회다`() {
        // given
        every {
            recordFeedService.searchRecordFeeds(requesterId = any(), request = any())
        } returns feedPage(nextCursor = null)
        every {
            recordService.fetchRecord(requesterId = any(), recordId = DummyRecord.ID)
        } returns DummyRecord.toDetailResponse()

        // when & then
        mockMvc.perform(get("/api/v1/records/feed")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/records/${DummyRecord.ID}")).andExpect(status().isOk)

        verify(exactly = 1) { recordFeedService.searchRecordFeeds(requesterId = any(), request = any()) }
        verify(exactly = 1) { recordService.fetchRecord(requesterId = any(), recordId = DummyRecord.ID) }
    }
}
