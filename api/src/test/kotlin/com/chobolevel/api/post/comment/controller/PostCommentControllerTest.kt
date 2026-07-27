package com.chobolevel.api.post.comment.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyPostComment
import com.chobolevel.api.common.dummy.DummyUser
import com.chobolevel.api.common.posttask.CreatePostCommentPostTask
import com.chobolevel.api.post.comment.service.PostCommentService
import com.chobolevel.api.post.comment.validator.PostCommentParameterValidator
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

@WebMvcTest(PostCommentController::class)
@Import(PostCommentControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("PostCommentController 슬라이스 테스트")
class PostCommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var postCommentService: PostCommentService

    @MockkBean
    private lateinit var postCommentParameterValidator: PostCommentParameterValidator

    @MockkBean
    private lateinit var createPostCommentPostTask: CreatePostCommentPostTask

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
    @WithMockUser(username = "1")
    fun `인증된 사용자가 유효한 요청으로 댓글을 등록하면 댓글 id를 반환한다`() {
        // given
        every { postCommentService.createPostComment(userId = DummyUser.ID, request = any()) } returns DummyPostComment.ID
        justRun { createPostCommentPostTask.invoke() }

        // when & then
        mockMvc.perform(
            post("/api/v1/posts/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyPostComment.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyPostComment.ID))
    }

    @Test
    fun `미인증 사용자가 댓글 등록을 시도하면 401을 반환한다`() {
        mockMvc.perform(
            post("/api/v1/posts/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyPostComment.toCreateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1")
    fun `내용이 빈 문자열인 댓글 등록 요청은 400을 반환한다`() {
        val invalidRequest = DummyPostComment.toCreateRequest().copy(content = "")

        mockMvc.perform(
            post("/api/v1/posts/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("댓글 내용은 필수 값입니다."))
    }

    @Test
    fun `인증 없이 댓글 목록을 조회할 수 있다`() {
        // given
        every { postCommentService.searchPostComments(filter = any(), pageRequest = any()) } returns
            PagingResponse(
                page = 1L,
                size = 50L,
                data = listOf(DummyPostComment.toResponse()),
                totalCount = 1L
            )

        // when & then
        mockMvc.perform(get("/api/v1/posts/comments"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.total_count").value(1L))
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    @WithMockUser(username = "1")
    fun `인증된 사용자가 댓글을 수정하면 댓글 id를 반환한다`() {
        // given
        justRun { postCommentParameterValidator.validate(request = any()) }
        every {
            postCommentService.updatePostComment(
                userId = DummyUser.ID,
                postCommentId = DummyPostComment.ID,
                request = any()
            )
        } returns DummyPostComment.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/posts/comments/${DummyPostComment.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyPostComment.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyPostComment.ID))
    }

    @Test
    fun `미인증 사용자가 댓글 수정을 시도하면 401을 반환한다`() {
        mockMvc.perform(
            put("/api/v1/posts/comments/${DummyPostComment.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyPostComment.toUpdateRequest()))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1")
    fun `인증된 사용자가 댓글을 삭제하면 true를 반환한다`() {
        // given
        every {
            postCommentService.deletePostComment(
                userId = DummyUser.ID,
                postCommentId = DummyPostComment.ID
            )
        } returns true

        // when & then
        mockMvc.perform(delete("/api/v1/posts/comments/${DummyPostComment.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `미인증 사용자가 댓글 삭제를 시도하면 401을 반환한다`() {
        mockMvc.perform(delete("/api/v1/posts/comments/${DummyPostComment.ID}"))
            .andExpect(status().isUnauthorized)
    }
}
