package com.chobolevel.api.post.controller

import com.chobolevel.api.common.dummy.DummyPost
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.post.service.PostService
import com.chobolevel.api.post.validator.PostParameterValidator
import com.fasterxml.jackson.databind.ObjectMapper
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

// @WebMvcTest 슬라이스 테스트: Controller 계층만 Spring 컨텍스트에 로드한다.
// 로드됨  → Controller, ControllerAdvice, WebMvcConfigurer, Security AutoConfig
// 제외됨  → Service, Repository, JPA 인프라 등 나머지 모든 빈
// 결과적으로 Service/Validator 같은 의존성은 반드시 @MockkBean으로 대체해야 한다.
@WebMvcTest(PostController::class)
@Import(PostControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("PostController 슬라이스 테스트")
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // @WebMvcTest는 Service를 로드하지 않으므로 MockkBean으로 대체한다.
    // "이 메서드가 호출되면 이 값을 반환해라"만 정의하면 된다.
    @MockkBean
    private lateinit var postService: PostService

    @MockkBean
    private lateinit var postParameterValidator: PostParameterValidator

    // @WebMvcTest는 프로덕션의 WebConfiguration(@EnableWebSecurity)을 로드하지 않는다.
    // 대신 Spring Security의 기본 설정(CSRF 활성, 모든 요청 인증 필요)이 적용되어
    // 공개 GET 엔드포인트도 401, POST는 CSRF 토큰 없으면 403이 된다.
    // → 프로덕션과 동일한 조건으로 테스트하려면 FilterChain을 직접 정의해야 한다.
    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize("hasRole('USER')") 등이 동작하려면 필요
    class TestSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain =
            http
                .csrf { it.disable() }           // 비활성화하지 않으면 POST 요청이 CSRF 토큰 없이 403
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { it.anyRequest().permitAll() } // URL 레벨은 전부 허용, 인가는 @PreAuthorize가 담당
                .build()
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    // @WithMockUser: JWT 필터 없이 "이 요청은 username=1인 ROLE_USER 사용자가 보낸 것"으로 처리
    // principal.name = "1" → getUserId() = 1L
    @WithMockUser(username = "1")
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
        // @PreAuthorize("hasRole('USER')") 실패 → AccessDeniedException
        // → ExceptionHandler.handleAccessDeniedException() → 401 (프로젝트 설계)
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
        // @Valid + @NotEmpty 검증 실패 → MethodArgumentNotValidException
        // → ExceptionHandler.methodArgumentNotValidExceptionHandler() → 400
        val invalidRequest = DummyPost.toCreateRequest().copy(title = "")

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
