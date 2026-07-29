package com.chobolevel.api.guest.controller

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dummy.DummyGuestBook
import com.chobolevel.api.common.posttask.CreateGuestBookPostTask
import com.chobolevel.api.guest.dto.UpdateGuestBookRequest
import com.chobolevel.api.guest.service.GuestBookService
import com.chobolevel.api.guest.validator.GuestBookParameterValidator
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GuestBookController::class)
@Import(GuestBookControllerTest.TestSecurityConfig::class)
@ActiveProfiles("test")
@DisplayName("GuestBookController 슬라이스 테스트")
class GuestBookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var guestBookService: GuestBookService

    @MockkBean
    private lateinit var guestBookParameterValidator: GuestBookParameterValidator

    // @Component이지만 @Value로 Discord 설정을 주입받으므로 Mock으로 대체한다.
    // 실제 빈이 생성되지 않아 암호화된 프로퍼티 복호화 없이도 컨텍스트가 기동된다.
    @MockkBean
    private lateinit var createGuestBookPostTask: CreateGuestBookPostTask

    @TestConfiguration
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
    fun `유효한 요청으로 방명록 등록 시 200과 id를 반환한다`() {
        // given
        every { guestBookService.createGuestBook(request = any()) } returns DummyGuestBook.ID
        justRun { createGuestBookPostTask.invoke() }

        // when & then
        mockMvc.perform(
            post("/api/v1/guest-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyGuestBook.toCreateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyGuestBook.ID))
    }

    @Test
    fun `작성자 이름이 비어있는 방명록 등록 요청은 400을 반환한다`() {
        // given — @Valid + @NotEmpty 검증 실패
        val invalidRequest = DummyGuestBook.toCreateRequest().copy(guestName = "")

        // when & then
        mockMvc.perform(
            post("/api/v1/guest-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("방문록 작성자 이름은 필수 값입니다."))
    }

    @Test
    fun `방명록 목록 조회 시 200과 PagingResponse를 반환한다`() {
        // given
        every {
            guestBookService.searchGuestBooks(filter = any(), pageRequest = any())
        } returns PagingResponse(
            page = 1L,
            size = 10L,
            data = listOf(DummyGuestBook.toResponse()),
            totalCount = 1L
        )

        // when & then
        mockMvc.perform(get("/api/v1/guest-books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.total_count").value(1L))
            .andExpect(jsonPath("$.data.data").isArray)
    }

    @Test
    fun `방명록 단건 조회 시 200과 GuestBookResponse를 반환한다`() {
        // given
        every { guestBookService.fetchGuestBook(id = DummyGuestBook.ID) } returns DummyGuestBook.toResponse()

        // when & then
        mockMvc.perform(get("/api/v1/guest-books/${DummyGuestBook.ID}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(DummyGuestBook.ID))
            .andExpect(jsonPath("$.data.guest_name").value(DummyGuestBook.GUEST_NAME))
    }

    @Test
    fun `유효한 요청으로 방명록 수정 시 200과 id를 반환한다`() {
        // given
        justRun { guestBookParameterValidator.validate(request = any<UpdateGuestBookRequest>()) }
        every {
            guestBookService.updateGuestBook(id = DummyGuestBook.ID, request = DummyGuestBook.toUpdateRequest())
        } returns DummyGuestBook.ID

        // when & then
        mockMvc.perform(
            put("/api/v1/guest-books/${DummyGuestBook.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyGuestBook.toUpdateRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(DummyGuestBook.ID))
    }

    @Test
    fun `비밀번호가 비어있는 방명록 수정 요청은 400을 반환한다`() {
        // given — @Valid + @NotEmpty 검증 실패
        val invalidRequest = DummyGuestBook.toUpdateRequest().copy(password = "")

        // when & then
        mockMvc.perform(
            put("/api/v1/guest-books/${DummyGuestBook.ID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("방문록 수정 시 비밀번호는 필수 값입니다."))
    }

    @Test
    fun `유효한 요청으로 방명록 삭제 시 200과 true를 반환한다`() {
        // given
        every {
            guestBookService.deleteGuestBook(id = DummyGuestBook.ID, request = DummyGuestBook.toDeleteRequest())
        } returns true

        // when & then
        mockMvc.perform(
            put("/api/v1/guest-books/${DummyGuestBook.ID}/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DummyGuestBook.toDeleteRequest()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value(true))
    }

    @Test
    fun `비밀번호가 비어있는 방명록 삭제 요청은 400을 반환한다`() {
        // given — @Valid + @NotEmpty 검증 실패
        val invalidRequest = DummyGuestBook.toDeleteRequest().copy(password = "")

        // when & then
        mockMvc.perform(
            put("/api/v1/guest-books/${DummyGuestBook.ID}/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_message").value("비밀번호는 필수 값입니다."))
    }
}
