package com.chobolevel.api.post.controller

import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.post.service.PostService
import com.chobolevel.api.post.validator.PostParameterValidator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PostController::class)
@Import(PostControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("PostController 슬라이스 테스트")
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var postService: PostService

    @MockkBean
    private lateinit var postParameterValidator: PostParameterValidator

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    class TestSecurityConfig {
        // WebConfiguration이 @WebMvcTest에서 로드되지 않으므로 최소 FilterChain을 직접 정의
        // CSRF 비활성화 + 모든 요청 허용 → @PreAuthorize가 실제 인가 관문 역할을 함
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
        io.mockk.clearAllMocks()
    }

    @Test
    @WithMockUser(username = "1") // principal.name = "1" → getUserId() = 1L
    fun `인증된 사용자가 유효한 요청으로 게시글을 등록하면 게시글 id를 반환한다`() {
        // given
        every { postService.createPost(userId = DummyUser.ID, request = any()) } returns DummyPost.ID

        // when & then
        mockMvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyPost.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyPost.ID))
    }

    @Test
    fun `미인증 사용자가 게시글 등록을 시도하면 401을 반환한다`() {
        // when & then
        // AccessDeniedException → ExceptionHandler → 401 (프로젝트 설계)
        mockMvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyPost.toCreateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1")
    fun `제목이 빈 문자열인 게시글 등록 요청은 400을 반환한다`() {
        // given - @NotEmpty 검증 실패 케이스
        val invalidRequest = DummyPost.toCreateRequest().copy(title = "")

        // when & then
        mockMvc.perform(
            post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("게시글 제목은 필수 값입니다."))
    }

    @Test
    fun `인증 없이 게시글 목록을 조회할 수 있다`() {
        // given
        every { postService.searchPosts(filter = any(), pageRequest = any()) } returns
            com.chobolevel.api.common.dto.PagingResponse(
                page = 1L,
                size = 20L,
                data = listOf(DummyPost.toResponse()),
                totalCount = 1L
            )

        // when & then
        mockMvc.perform(get("/api/v1/posts"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.total_count").value(1L))
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    fun `인증 없이 게시글 단건을 조회할 수 있다`() {
        // given
        every { postService.fetchPost(postId = DummyPost.ID) } returns DummyPost.toResponse()

        // when & then
        mockMvc.perform(get("/api/v1/posts/${DummyPost.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyPost.ID))
            .andExpect(jsonPath("$.data.title").value(DummyPost.TITLE))
    }
}
