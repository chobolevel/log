package com.chobolevel.api.record.emotion.controller

import com.chobolevel.api.common.dummy.DummyRecord
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.record.emotion.service.RecordEmotionService
import com.chobolevel.api.record.emotion.validator.RecordEmotionParameterValidator
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecordEmotionController::class)
@Import(RecordEmotionControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("RecordEmotionController 슬라이스 테스트")
class RecordEmotionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var recordEmotionService: RecordEmotionService

    @MockkBean
    private lateinit var recordEmotionParameterValidator: RecordEmotionParameterValidator

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
    fun `인증된 사용자가 기록 감정 수정 요청 시 기록 감정 수정 후 id를 반환한다`() {
        // given
        justRun { recordEmotionParameterValidator.validate(request = any()) }
        every {
            recordEmotionService.updateRecordEmotion(
                userId = DummyUser.ID,
                recordEmotionId = DummyRecord.RECORD_EMOTION_ID,
                request = any()
            )
        } returns DummyRecord.RECORD_EMOTION_ID

        // when & then
        mockMvc.perform(
            put("/api/v1/record-emotions/${DummyRecord.RECORD_EMOTION_ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyRecord.toUpdateEmotionRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyRecord.RECORD_EMOTION_ID))
    }

    @Test
    fun `인증 없이 기록 감정 수정 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            put("/api/v1/record-emotions/${DummyRecord.RECORD_EMOTION_ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyRecord.toUpdateEmotionRequest()))
        )
            .andExpect(status().isUnauthorized)
    }
}
