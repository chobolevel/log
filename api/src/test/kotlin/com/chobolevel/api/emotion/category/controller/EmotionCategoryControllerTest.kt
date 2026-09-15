package com.chobolevel.api.emotion.category.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyEmotionCategory
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.emotion.category.dto.EmotionCategoryResponse
import com.chobolevel.api.emotion.category.service.EmotionCategoryService
import com.chobolevel.api.emotion.category.validator.EmotionCategoryParameterValidator
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

@WebMvcTest(EmotionCategoryController::class)
@Import(EmotionCategoryControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("EmotionCategoryController 슬라이스 테스트")
class EmotionCategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var emotionCategoryService: EmotionCategoryService

    @MockkBean
    private lateinit var emotionCategoryParameterValidator: EmotionCategoryParameterValidator

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
    fun `인증된 관리자가 감정 카테고리 등록 요청 시 감정 카테고리 등록 후 id를 반환한다`() {
        // given
        every { emotionCategoryService.createEmotionCategory(request = any()) } returns DummyEmotionCategory.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/emotion-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyEmotionCategory.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyEmotionCategory.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 감정 카테고리 등록 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            post("/api/v1/emotion-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyEmotionCategory.toCreateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `인증 없이 감정 카테고리 목록을 조회할 수 있다`() {
        // given
        every {
            emotionCategoryService.searchEmotionCategories(request = any())
        } returns PagingResponse(
            page = 1L,
            size = 20L,
            data = listOf<EmotionCategoryResponse>(DummyEmotionCategory.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(
            get("/api/v1/emotion-categories")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.data").isArray)
            .andExpect(jsonPath("$.data.total_count").value(1L))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 감정 카테고리 수정 요청 시 감정 카테고리 수정 후 id를 반환한다`() {
        // given
        justRun { emotionCategoryParameterValidator.validate(request = any()) }
        every {
            emotionCategoryService.updateEmotionCategory(
                emotionCategoryId = any(),
                request = any()
            )
        } returns DummyEmotionCategory.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/emotion-categories/${DummyEmotionCategory.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyEmotionCategory.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyEmotionCategory.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 감정 카테고리 수정 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            put("/api/v1/emotion-categories/${DummyEmotionCategory.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyEmotionCategory.toUpdateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 감정 카테고리 삭제 요청 시 감정 카테고리 삭제 후 true를 반환한다`() {
        // given
        every { emotionCategoryService.deleteEmotionCategory(emotionCategoryId = DummyEmotionCategory.ID) } returns true

        // when & then
        mockMvc.perform(
            delete("/api/v1/emotion-categories/${DummyEmotionCategory.ID}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 감정 카테고리 삭제 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            delete("/api/v1/emotion-categories/${DummyEmotionCategory.ID}")
        )
            .andExpect(status().isUnauthorized)
    }
}
