package com.chobolevel.api.api.posts.controller

import com.chobolevel.api.common.config.TestSecurityConfig
import com.chobolevel.api.common.dummy.posts.DummyPost
import com.chobolevel.api.common.dto.PaginationResponseDto
import com.chobolevel.api.post.controller.PostController
import com.chobolevel.api.post.dto.PostResponseDto
import com.chobolevel.api.post.query.PostQueryCreator
import com.chobolevel.api.post.service.PostService
import com.chobolevel.api.post.validator.PostParameterValidator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

// [컨트롤러 통합 테스트 특징]
// - @WebMvcTest: DispatcherServlet, Filter, Interceptor 등 MVC 계층만 로드한다.
//   전체 Spring 컨텍스트(@SpringBootTest)보다 훨씬 빠르게 실행된다.
// - 검증 범위: HTTP 상태 코드, URL 라우팅, Request/Response JSON 직렬화, 인증/인가
// - Service 계층은 @MockkBean으로 대체하여 컨트롤러 로직만 검증한다.
// - @Import(TestSecurityConfig::class): JWT 의존성 없이 @WithMockUser로 인증 상태를 주입한다.
// - @Import(PostQueryCreator::class, PostParameterValidator::class):
//   의존성이 없는 컴포넌트는 실제 Bean으로 주입하여 실제 변환/검증 로직도 함께 검증한다.
@DisplayName("게시글 컨트롤러 통합 테스트")
@WebMvcTest(controllers = [PostController::class])
@Import(TestSecurityConfig::class, PostQueryCreator::class, PostParameterValidator::class)
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // @MockkBean: Spring 컨텍스트에 MockK 기반의 가짜 Bean을 등록한다.
    // (springmockk 라이브러리의 @MockBean과 동일한 역할, MockK 문법 사용 가능)
    @MockkBean
    private lateinit var service: PostService

    // ==================== POST /api/v1/posts ====================

    @Test
    @DisplayName("인증된 사용자가 게시글을 등록하면 생성된 게시글 ID를 반환한다")
    // @WithMockUser: SecurityContextHolder에 username="1", roles=["USER"] 인증 객체를 주입한다.
    // principal.getUserId()는 principal.name.toLong()이므로 username과 userId가 일치해야 한다.
    @WithMockUser(username = "1", roles = ["USER"])
    fun `게시글_등록_성공`() {
        // given
        every { service.createPost(userId = 1L, request = any()) } returns DummyPost.ID

        // when & then
        mockMvc.post("/api/v1/posts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(DummyPost.toCreateRequestDto())
        }.andExpect {
            status { isOk() }
            // ResultResponse(data = postId) → {"data": 1}
            jsonPath("$.data") { value(DummyPost.ID) }
        }

        verify(exactly = 1) { service.createPost(userId = 1L, request = any()) }
    }

    @Test
    @DisplayName("인증 없이 게시글 등록 시 403을 반환한다")
    fun `게시글_등록_인증_없음_403`() {
        // @HasAuthorityUser = @PreAuthorize("hasRole('USER')")
        // 미인증(anonymous) 사용자가 접근하면 AccessDeniedException → 403 Forbidden
        mockMvc.post("/api/v1/posts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(DummyPost.toCreateRequestDto())
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @DisplayName("필수 필드 누락 시 400을 반환한다")
    @WithMockUser(username = "1", roles = ["USER"])
    fun `게시글_등록_제목_누락_400`() {
        // given: title이 빈 문자열 → @NotEmpty 검증 실패
        val invalidRequest: Map<String, Any?> = mapOf(
            "tag_ids" to listOf(1L),
            "title" to "",
            "sub_title" to "부제목",
            "content" to "내용"
        )

        // when & then
        // @Valid + @NotEmpty 검증 실패 → MethodArgumentNotValidException → 400 Bad Request
        mockMvc.post("/api/v1/posts") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // ==================== GET /api/v1/posts ====================

    @Test
    @DisplayName("게시글 목록을 조회하면 페이징 결과를 반환한다")
    fun `게시글_목록_조회_성공`() {
        // given
        val paginationResponse: PaginationResponseDto = PaginationResponseDto(
            totalCount = 1L,
            skipCount = 0L,
            limitCount = 20L,
            data = listOf(DummyPost.toResponseDto())
        )
        every {
            service.searchPosts(queryFilter = any(), pagination = any(), orderTypes = any())
        } returns paginationResponse

        // when & then
        // @JsonNaming(SnakeCaseStrategy) 적용으로 totalCount → total_count 직렬화
        mockMvc.get("/api/v1/posts") {
            param("skip_count", "0")
            param("limit_count", "20")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.total_count") { value(1) }
            jsonPath("$.data.skip_count") { value(0) }
            jsonPath("$.data.limit_count") { value(20) }
        }
    }

    // ==================== GET /api/v1/posts/{id} ====================

    @Test
    @DisplayName("게시글 단건을 조회하면 게시글 상세 정보를 반환한다")
    fun `게시글_단건_조회_성공`() {
        // given
        val postResponse: PostResponseDto = DummyPost.toResponseDto()
        every { service.fetchPost(postId = DummyPost.ID) } returns postResponse

        // when & then
        mockMvc.get("/api/v1/posts/${DummyPost.ID}")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(DummyPost.ID) }
                jsonPath("$.data.title") { value(DummyPost.TITLE) }
                jsonPath("$.data.sub_title") { value(DummyPost.SUB_TITLE) }
            }
    }

    // ==================== PUT /api/v1/posts/{id} ====================

    @Test
    @DisplayName("인증된 사용자가 게시글을 수정하면 게시글 ID를 반환한다")
    @WithMockUser(username = "1", roles = ["USER"])
    fun `게시글_수정_성공`() {
        // given
        every {
            service.updatePost(userId = 1L, postId = DummyPost.ID, request = any())
        } returns DummyPost.ID

        // when & then
        mockMvc.put("/api/v1/posts/${DummyPost.ID}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(DummyPost.toUpdateRequestDto())
        }.andExpect {
            status { isOk() }
            jsonPath("$.data") { value(DummyPost.ID) }
        }
    }

    // ==================== DELETE /api/v1/posts/{id} ====================

    @Test
    @DisplayName("인증된 사용자가 게시글을 삭제하면 true를 반환한다")
    @WithMockUser(username = "1", roles = ["USER"])
    fun `게시글_삭제_성공`() {
        // given
        every { service.deletePost(userId = 1L, postId = DummyPost.ID) } returns true

        // when & then
        mockMvc.delete("/api/v1/posts/${DummyPost.ID}")
            .andExpect {
                status { isOk() }
                jsonPath("$.data") { value(true) }
            }
    }

    @Test
    @DisplayName("인증 없이 게시글 삭제 시 403을 반환한다")
    fun `게시글_삭제_인증_없음_403`() {
        mockMvc.delete("/api/v1/posts/${DummyPost.ID}")
            .andExpect {
                status { isForbidden() }
            }
    }
}
