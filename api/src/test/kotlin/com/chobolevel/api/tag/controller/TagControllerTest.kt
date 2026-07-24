package com.chobolevel.api.tag.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyTag
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.tag.service.TagService
import com.chobolevel.api.tag.validator.TagParameterValidator
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

@WebMvcTest(TagController::class)
@Import(TagControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("TagController 슬라이스 테스트")
class TagControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var tagService: TagService

    @MockkBean
    private lateinit var tagParameterValidator: TagParameterValidator

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain =
            http
                .csrf { it.disable() } // 비활성화하지 않으면 POST 요청이 CSRF 토큰 없이 403
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { it.anyRequest().permitAll() } // URL 레벨은 전부 허용, 인가는 @PreAuthorize가 담당
                .build()
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 태그 등록 요청 시 태그 등록 후 ID를 반환한다`() {
        // given
        every { tagService.createTag(request = any()) } returns DummyTag.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyTag.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyTag.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 태그 등록 요청 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            post("/api/v1/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyTag.toCreateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `인증 없이 태그 목록을 조회할 수 있다`() {
        // given
        every {
            tagService.searchTags(
                filter = any(),
                pageRequest = any()
            )
        } returns PagingResponse(
            page = 1L,
            size = 20L,
            data = listOf(DummyTag.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(
            get("/api/v1/tags")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.data").isArray)
            .andExpect(jsonPath("$.data.total_count").value(1L))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 태그 수정 요청 시 태그 수정 후 태그 id를 반환한다`() {
        // given
        justRun { tagParameterValidator.validate(request = any()) }
        every {
            tagService.updateTag(
                tagId = any(),
                request = any()
            )
        } returns DummyTag.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/tags/${DummyTag.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyTag.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyTag.ID))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 태그 수정 시 401을 반환한다`() {
        // given & when & then
        mockMvc.perform(
            put("/api/v1/tags/${DummyTag.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyTag.toUpdateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["ADMIN"])
    fun `인증된 관리자가 태그 삭제 요청 시 태그 삭제 후 true 반환한다`() {
        // given
        every { tagService.deleteTag(tagId = DummyTag.ID) } returns true

        // when & then
        mockMvc.perform(
            delete("/api/v1/tags/${DummyTag.ID}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    @WithMockUser(username = "${DummyUser.ID}", roles = ["USER"])
    fun `인증된 사용자가 태그 삭제 요청 시 401 반환한다`() {
        // given & when & then
        mockMvc.perform(
            delete("/api/v1/tags/${DummyTag.ID}")
        )
            .andExpect(status().isUnauthorized)
    }
}
