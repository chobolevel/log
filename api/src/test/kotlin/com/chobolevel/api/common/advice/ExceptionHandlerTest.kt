package com.chobolevel.api.common.advice

import com.chobolevel.api.common.exception.ErrorCodeScanner
import com.chobolevel.api.common.security.AuthErrorCode
import com.chobolevel.domain.common.exception.BusinessErrorCode
import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.common.exception.CommonErrorCode
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ErrorType
import com.chobolevel.domain.common.exception.ExternalSystemException
import com.chobolevel.domain.common.exception.InternalSystemException
import com.chobolevel.domain.common.exception.SystemErrorCode
import com.chobolevel.domain.emotion.exception.EmotionErrorCode
import com.chobolevel.domain.notification.exception.NotificationErrorCode
import com.chobolevel.domain.record.exception.RecordErrorCode
import com.chobolevel.domain.subject.exception.SubjectErrorCode
import com.chobolevel.domain.user.exception.UserErrorCode
import io.kotest.matchers.shouldBe
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Stream

// [특성화 테스트] 리팩터링 전 ExceptionHandler의 "현재 동작"을 고정한다.
// 옳고 그름을 판정하는 테스트가 아니라, 이후 단계에서 바뀌는 지점이 테스트 diff로 드러나게 하는 안전망이다.
// 의도적으로 바뀔 동작에는 "(현재 동작)"이라고 적어 두었다.
@WebMvcTest(ExceptionHandlerTest.ThrowingController::class)
// 테스트 클래스에 중첩된 클래스는 컴포넌트 스캔에서 제외되므로 테스트용 컨트롤러를 명시적으로 등록한다
@Import(ExceptionHandlerTest.TestSecurityConfig::class, ExceptionHandlerTest.ThrowingController::class)
@ActiveProfiles("test")
@DisplayName("ExceptionHandler 특성화 테스트")
class ExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

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

    // 요청 파라미터로 지정한 예외를 그대로 던지는 테스트 전용 컨트롤러
    @RestController
    class ThrowingController {

        @GetMapping("/test/throw")
        fun throwCustom(@RequestParam thrower: Thrower, @RequestParam code: String): String {
            val errorCode: ErrorCode = ErrorCodeScanner.findAll().first { it.name == code }
            throw thrower.create(errorCode)
        }

        @GetMapping("/test/illegal-argument")
        fun illegalArgument(): String = throw IllegalArgumentException("엔티티 불변식 위반")

        @GetMapping("/test/data-integrity")
        fun dataIntegrity(): String = throw DataIntegrityViolationException("duplicate key")

        @GetMapping("/test/access-denied")
        fun accessDenied(): String = throw AccessDeniedException("Access Denied")

        @GetMapping("/test/bad-credentials")
        fun badCredentials(): String = throw BadCredentialsException("Bad credentials")

        @GetMapping("/test/runtime-error")
        fun runtimeError(): String = throw RuntimeException("DB 접속 정보 오류: jdbc:mysql://internal-host/log")

        @PostMapping("/test/body")
        fun body(
            @Valid @RequestBody
            request: BodyRequest
        ): String = "ok"

        @GetMapping("/test/required-param")
        fun requiredParam(@RequestParam id: Long): String = "ok"

        @GetMapping("/test/typed/{id}")
        fun typedPath(@PathVariable id: Long): String = "ok"

        @GetMapping("/test/json-only", produces = [MediaType.APPLICATION_JSON_VALUE])
        fun jsonOnly(): String = "ok"

        @GetMapping("/test/model-attribute")
        fun modelAttribute(request: AgeRequest): String = "ok"
    }

    data class AgeRequest(val age: Int?)

    data class BodyRequest(
        @field:NotBlank(message = "이름은 필수 값입니다.")
        val name: String?
    )

    // 던지는 예외 종류. 비즈니스 예외의 상태는 errorCode.type이, 시스템 예외의 상태는 클래스가 결정한다.
    enum class Thrower(val create: (ErrorCode) -> Throwable) {
        BUSINESS({ BusinessException(errorCode = it as BusinessErrorCode) }),
        INTERNAL_SYSTEM({ InternalSystemException(errorCode = it as SystemErrorCode) }),
        EXTERNAL_SYSTEM({ ExternalSystemException(errorCode = it as SystemErrorCode) })
    }

    // ===== 에러코드별 HTTP 상태 표 =====

    @ParameterizedTest(name = "{1} ({0}) -> {2}")
    @MethodSource("errorCodeStatusTable")
    @DisplayName("에러코드를 현재 던지는 예외로 던지면 표의 상태 코드로 응답한다")
    fun `에러코드별 HTTP 상태`(thrower: Thrower, code: ErrorCode, expectedStatus: Int) {
        mockMvc.perform(get("/test/throw").param("thrower", thrower.name).param("code", code.name))
            .andExpect(status().`is`(expectedStatus))
            .andExpect(jsonPath("$.error_code").value(code.name))
            .andExpect(jsonPath("$.error_message").value(code.defaultMessage))
    }

    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @MethodSource("errorTypeRepresentatives")
    @DisplayName("BusinessException은 errorCode.type에 따라 상태 코드가 정해진다")
    fun `BusinessException의 ErrorType별 HTTP 상태`(type: ErrorType, code: ErrorCode, expectedStatus: Int) {
        mockMvc.perform(get("/test/throw").param("thrower", Thrower.BUSINESS.name).param("code", code.name))
            .andExpect(status().`is`(expectedStatus))
            .andExpect(jsonPath("$.error_code").value(code.name))
    }

    @Test
    @DisplayName("모든 ErrorType은 대표 코드로 검증되고 있다")
    fun `새 ErrorType이 추가되면 대표 코드 검증이 빠져 실패한다`() {
        val verifiedTypes: Set<ErrorType> = errorTypeRepresentatives
            .map { (type: ErrorType, _, _) -> type }
            .toSet()

        verifiedTypes shouldBe ErrorType.values().toSet()
    }

    @Test
    @DisplayName("모든 에러코드는 상태 표 또는 핸들러 직접 처리 목록에 포함돼 있다")
    fun `새 에러코드가 표에서 빠지면 실패한다`() {
        val covered: Set<String> = (tableCodes() + handlerDirectCodes).map { it.name }.toSet()

        val missing: List<String> = ErrorCodeScanner.findAll().map { it.name }.filter { it !in covered }

        missing shouldBe emptyList()
    }

    // ===== 핸들러가 직접 처리하는 예외 =====

    @Test
    @DisplayName("IllegalArgumentException은 400 INVALID_PARAMETER로 응답하고 예외 메시지를 노출한다")
    fun `IllegalArgumentException 처리`() {
        mockMvc.perform(get("/test/illegal-argument"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.INVALID_PARAMETER.name))
            .andExpect(jsonPath("$.error_message").value("엔티티 불변식 위반"))
    }

    @Test
    @DisplayName("DataIntegrityViolationException은 409 DUPLICATE_REQUEST로 응답한다")
    fun `DataIntegrityViolationException 처리`() {
        mockMvc.perform(get("/test/data-integrity"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.DUPLICATE_REQUEST.name))
            .andExpect(jsonPath("$.error_message").value(CommonErrorCode.DUPLICATE_REQUEST.defaultMessage))
    }

    @Test
    @DisplayName("AccessDeniedException은 인증되지 않은 요청이면 401 AUTHENTICATION_REQUIRED로 응답한다")
    fun `익명 요청의 AccessDeniedException 처리`() {
        mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error_code").value(AuthErrorCode.AUTHENTICATION_REQUIRED.name))
            .andExpect(jsonPath("$.error_message").value(AuthErrorCode.AUTHENTICATION_REQUIRED.defaultMessage))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    @DisplayName("AccessDeniedException은 인증된 요청이면 403 ACCESS_DENIED로 응답하고 영문 기본 메시지를 노출하지 않는다")
    fun `인증된 요청의 AccessDeniedException 처리`() {
        mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.ACCESS_DENIED.name))
            .andExpect(jsonPath("$.error_message").value(CommonErrorCode.ACCESS_DENIED.defaultMessage))
    }

    @Test
    @DisplayName("Spring Security의 BadCredentialsException은 401 BAD_CREDENTIALS로 응답하고 영문 기본 메시지를 노출하지 않는다")
    fun `BadCredentialsException 처리`() {
        mockMvc.perform(get("/test/bad-credentials"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error_code").value(AuthErrorCode.BAD_CREDENTIALS.name))
            .andExpect(jsonPath("$.error_message").value(AuthErrorCode.BAD_CREDENTIALS.defaultMessage))
    }

    @Test
    @DisplayName("@Valid 검증에 실패하면 400 INVALID_PARAMETER로 응답하고 첫 번째 검증 메시지를 노출한다")
    fun `MethodArgumentNotValidException 처리`() {
        mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("""{"name": ""}"""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.INVALID_PARAMETER.name))
            .andExpect(jsonPath("$.error_message").value("이름은 필수 값입니다."))
    }

    @Test
    @DisplayName("읽을 수 없는 JSON 본문이면 400 INVALID_REQUEST_FORMAT으로 응답한다")
    fun `HttpMessageNotReadableException 처리`() {
        mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.INVALID_REQUEST_FORMAT.name))
            .andExpect(jsonPath("$.error_message").value(CommonErrorCode.INVALID_REQUEST_FORMAT.defaultMessage))
    }

    @Test
    @DisplayName("처리되지 않은 예외는 500 INTERNAL_SERVER_ERROR로 응답하고 내부 예외 메시지를 노출하지 않는다")
    fun `미처리 예외 처리`() {
        mockMvc.perform(get("/test/runtime-error"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.error_code").value(SystemErrorCode.INTERNAL_SERVER_ERROR.name))
            .andExpect(jsonPath("$.error_message").value(SystemErrorCode.INTERNAL_SERVER_ERROR.defaultMessage))
            .andExpect(content().string(not(containsString("internal-host"))))
    }

    // ===== Spring 표준 예외: 현재 catch-all(Exception) 핸들러에 걸리는지 확인 =====

    @Test
    @DisplayName("필수 쿼리 파라미터가 없으면 400 INVALID_PARAMETER로 응답한다")
    fun `필수 파라미터 누락`() {
        mockMvc.perform(get("/test/required-param"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.INVALID_PARAMETER.name))
            .andExpect(jsonPath("$.error_message").value(CommonErrorCode.INVALID_PARAMETER.defaultMessage))
    }

    @Test
    @DisplayName("경로 변수 타입이 맞지 않으면 400 INVALID_PARAMETER로 응답한다")
    fun `경로 변수 타입 불일치`() {
        mockMvc.perform(get("/test/typed/abc"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.INVALID_PARAMETER.name))
    }

    @Test
    @DisplayName("@ModelAttribute 바인딩 오류는 400 INVALID_PARAMETER로 응답하고 Spring의 영문 변환 오류 메시지를 노출하지 않는다")
    fun `모델 바인딩 타입 불일치`() {
        mockMvc.perform(get("/test/model-attribute").param("age", "abc"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error_code").value(CommonErrorCode.INVALID_PARAMETER.name))
            .andExpect(jsonPath("$.error_message").value(CommonErrorCode.INVALID_PARAMETER.defaultMessage))
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 405 METHOD_NOT_ALLOWED로 응답하고 Allow 헤더를 유지한다")
    fun `지원하지 않는 HTTP 메서드`() {
        mockMvc.perform(post("/test/required-param").param("id", "1"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(header().string("Allow", containsString("GET")))
            .andExpect(jsonPath("$.error_code").value(HttpErrorCode.METHOD_NOT_ALLOWED.name))
            .andExpect(jsonPath("$.error_message").value(HttpErrorCode.METHOD_NOT_ALLOWED.defaultMessage))
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 415 UNSUPPORTED_MEDIA_TYPE으로 응답한다")
    fun `지원하지 않는 미디어 타입`() {
        mockMvc.perform(post("/test/body").contentType(MediaType.TEXT_PLAIN).content("name=a"))
            .andExpect(status().isUnsupportedMediaType)
            .andExpect(jsonPath("$.error_code").value(HttpErrorCode.UNSUPPORTED_MEDIA_TYPE.name))
            .andExpect(jsonPath("$.error_message").value(HttpErrorCode.UNSUPPORTED_MEDIA_TYPE.defaultMessage))
    }

    @Test
    @DisplayName("요청한 응답 형식(Accept)을 제공할 수 없으면 406 NOT_ACCEPTABLE로 응답하고 본문은 JSON이다")
    fun `제공할 수 없는 Accept`() {
        mockMvc.perform(get("/test/json-only").accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotAcceptable)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error_code").value(HttpErrorCode.NOT_ACCEPTABLE.name))
    }

    @Test
    @DisplayName("Accept가 JSON이 아니어도 비즈니스 에러 응답은 JSON으로 렌더링된다")
    fun `비JSON Accept의 비즈니스 에러`() {
        mockMvc.perform(
            get("/test/throw")
                .param("thrower", Thrower.BUSINESS.name)
                .param("code", UserErrorCode.USER_NOT_FOUND.name)
                .accept(MediaType.APPLICATION_XML)
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error_code").value(UserErrorCode.USER_NOT_FOUND.name))
    }

    companion object {

        // 에러코드별로 응답되는 HTTP 상태. 비즈니스 코드는 errorCode.type으로, 시스템 코드는 예외 클래스로 정해진다.
        // "400 -> 409"처럼 적힌 항목은 예외 구조 개편 이전에는 400이었던 코드다.
        private val table: List<Triple<Thrower, Int, List<ErrorCode>>> = listOf(
            // ----- 400 INVALID -----
            Triple(
                Thrower.BUSINESS,
                400,
                listOf(
                    CommonErrorCode.INVALID_PARAMETER,
                    UserErrorCode.EMAIL_VERIFICATION_CODE_NOT_MATCHED,
                    UserErrorCode.USER_PASSWORD_NOT_MATCHED,
                    UserErrorCode.USER_PASSWORD_REUSING_NOT_ALLOWED,
                    UserErrorCode.RESET_USER_PASSWORD_CODE_NOT_EXISTS,
                    UserErrorCode.USER_FOLLOW_SELF_NOT_ALLOWED
                )
            ),
            // ----- 401 UNAUTHENTICATED -----
            Triple(
                Thrower.BUSINESS,
                401,
                listOf(
                    AuthErrorCode.INVALID_TOKEN,
                    AuthErrorCode.EXPIRED_TOKEN,
                    AuthErrorCode.BAD_CREDENTIALS,
                    AuthErrorCode.AUTHENTICATION_REQUIRED
                )
            ),
            // ----- 403 FORBIDDEN -----
            Triple(
                Thrower.BUSINESS,
                403,
                listOf(
                    RecordErrorCode.RESTRICTED_TO_RECORD_WRITER,
                    RecordErrorCode.PRIVATE_RECORD,
                    NotificationErrorCode.RESTRICTED_TO_NOTIFICATION_OWNER
                )
            ),
            // ----- 404 NOT_FOUND -----
            Triple(
                Thrower.BUSINESS,
                404,
                listOf(
                    UserErrorCode.USER_NOT_FOUND,
                    UserErrorCode.USER_IMAGE_NOT_FOUND,
                    UserErrorCode.USER_FOLLOW_SYNC_EVENT_NOT_FOUND,
                    UserErrorCode.USER_EMAIL_NOT_EXISTS, // 400 -> 404
                    RecordErrorCode.RECORD_NOT_FOUND,
                    RecordErrorCode.RECORD_REVIEW_NOT_FOUND,
                    RecordErrorCode.RECORD_LIKE_SYNC_EVENT_NOT_FOUND,
                    RecordErrorCode.RECORD_VIEW_SYNC_EVENT_NOT_FOUND,
                    RecordErrorCode.RECORD_EMOTION_NOT_FOUND,
                    SubjectErrorCode.SUBJECT_NOT_FOUND,
                    SubjectErrorCode.SUBJECT_IMAGE_NOT_FOUND,
                    EmotionErrorCode.EMOTION_CATEGORY_NOT_FOUND,
                    EmotionErrorCode.EMOTION_NOT_FOUND,
                    NotificationErrorCode.NOTIFICATION_NOT_FOUND,
                    NotificationErrorCode.NOTIFICATION_DISPATCH_EVENT_NOT_FOUND
                )
            ),
            // ----- 409 CONFLICT -----
            Triple(
                Thrower.BUSINESS,
                409,
                listOf(
                    UserErrorCode.USER_EMAIL_ALREADY_EXISTS, // 400 -> 409
                    UserErrorCode.USER_NICKNAME_ALREADY_EXISTS, // 400 -> 409
                    UserErrorCode.USER_FOLLOW_SYNC_EVENT_NOT_FAILED, // 400 -> 409
                    RecordErrorCode.RECORD_LIKE_SYNC_EVENT_NOT_FAILED, // 400 -> 409
                    RecordErrorCode.RECORD_VIEW_SYNC_EVENT_NOT_FAILED, // 400 -> 409
                    EmotionErrorCode.EMOTION_CATEGORY_IN_USE, // 400 -> 409
                    EmotionErrorCode.EMOTION_IN_USE // 400 -> 409
                )
            ),
            // ----- 5xx: 예외 클래스가 상태를 결정 -----
            Triple(
                Thrower.INTERNAL_SYSTEM,
                503,
                listOf(SystemErrorCode.LOCK_ACQUISITION_FAILED) // 400 -> 503
            ),
            Triple(
                Thrower.EXTERNAL_SYSTEM,
                502,
                listOf(SystemErrorCode.EMAIL_SEND_FAILED)
            )
        )

        // ErrorType별 대표 코드: BusinessException이 errorCode.type만으로 상태가 정해지는지 확인한다
        private val errorTypeRepresentatives: List<Triple<ErrorType, ErrorCode, Int>> = listOf(
            Triple(ErrorType.INVALID, CommonErrorCode.INVALID_PARAMETER, 400),
            Triple(ErrorType.UNAUTHENTICATED, AuthErrorCode.INVALID_TOKEN, 401),
            Triple(ErrorType.FORBIDDEN, RecordErrorCode.PRIVATE_RECORD, 403),
            Triple(ErrorType.NOT_FOUND, UserErrorCode.USER_NOT_FOUND, 404),
            Triple(ErrorType.CONFLICT, UserErrorCode.USER_EMAIL_ALREADY_EXISTS, 409)
        )

        @JvmStatic
        fun errorTypeRepresentatives(): Stream<Arguments> =
            errorTypeRepresentatives.map { (type, code, status) -> Arguments.of(type, code, status) }.stream()

        // 커스텀 예외 클래스가 아니라 핸들러가 직접 에러코드를 정해 응답하는 코드 (위 개별 테스트로 검증)
        private val handlerDirectCodes: List<ErrorCode> = listOf(
            CommonErrorCode.INVALID_REQUEST_FORMAT,
            CommonErrorCode.DUPLICATE_REQUEST,
            CommonErrorCode.ACCESS_DENIED,
            SystemErrorCode.INTERNAL_SERVER_ERROR,
            HttpErrorCode.PATH_NOT_FOUND,
            HttpErrorCode.METHOD_NOT_ALLOWED,
            HttpErrorCode.NOT_ACCEPTABLE,
            HttpErrorCode.UNSUPPORTED_MEDIA_TYPE
        )

        private fun tableCodes(): List<ErrorCode> = table.flatMap { (_, _, codes) -> codes }

        @JvmStatic
        fun errorCodeStatusTable(): Stream<Arguments> =
            table.flatMap { (thrower, status, codes) ->
                codes.map { code -> Arguments.of(thrower, code, status) }
            }.stream()
    }
}
