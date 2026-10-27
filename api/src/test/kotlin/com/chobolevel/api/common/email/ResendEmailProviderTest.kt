package com.chobolevel.api.common.email

import com.chobolevel.domain.common.exception.ExternalSystemException
import com.chobolevel.domain.common.exception.SystemErrorCode
import com.chobolevel.domain.common.provider.EmailProvider
import com.resend.Resend
import com.resend.core.exception.ResendException
import com.resend.services.emails.Emails
import com.resend.services.emails.model.CreateEmailOptions
import com.resend.services.emails.model.CreateEmailResponse
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-7): 이동 전에 ResendEmailProvider의 현재 계약을 고정한다.
// 이 클래스는 호출마다 Resend 클라이언트를 직접 만들기 때문에 생성자를 모킹해서 SDK 호출을 가로챈다(실제 네트워크 호출 없음).
@DisplayName("ResendEmailProvider 계약 테스트")
class ResendEmailProviderTest {

    private val emails: Emails = mockk()
    private val emailProvider: EmailProvider = ResendEmailProvider(ResendProperties(apiKey = "re_test"))

    @BeforeEach
    fun setUp() {
        mockkConstructor(Resend::class)
        every { anyConstructed<Resend>().emails() } returns emails
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(Resend::class)
    }

    @Test
    fun `메일을 발송하면 발신자, 수신자, 제목, 본문을 담아 Resend로 보낸다`() {
        // given
        val sent: CapturingSlot<CreateEmailOptions> = slot()
        every { emails.send(capture(sent)) } returns mockk<CreateEmailResponse>()

        // when
        emailProvider.sendEmail(to = "to@test.com", subject = "제목", content = "<p>내용</p>")

        // then
        verify(exactly = 1) { emails.send(any<CreateEmailOptions>()) }
        assertThat(sent.captured.from).isEqualTo("[초로] <no-reply@chobolevel.com>")
        assertThat(sent.captured.to).containsExactly("to@test.com")
        assertThat(sent.captured.subject).isEqualTo("제목")
        assertThat(sent.captured.html).isEqualTo("<p>내용</p>")
    }

    @Test
    fun `Resend 발송이 실패하면 EMAIL_SEND_FAILED 외부 시스템 예외로 바꾸고 원인을 보존한다`() {
        // given
        val failure: ResendException = ResendException("발송 실패")
        every { emails.send(any<CreateEmailOptions>()) } throws failure

        // when
        val exception: ExternalSystemException = assertThrows {
            emailProvider.sendEmail(to = "to@test.com", subject = "제목", content = "내용")
        }

        // then
        assertThat(exception.errorCode).isEqualTo(SystemErrorCode.EMAIL_SEND_FAILED)
        assertThat(exception.message).contains("to@test.com")
        assertThat(exception.cause).isSameAs(failure)
    }

    @Test
    fun `Resend 예외가 아닌 오류는 그대로 전파된다`() {
        // given
        every { emails.send(any<CreateEmailOptions>()) } throws IllegalStateException("예상치 못한 오류")

        // when & then
        assertThrows<IllegalStateException> {
            emailProvider.sendEmail(to = "to@test.com", subject = "제목", content = "내용")
        }
    }
}
