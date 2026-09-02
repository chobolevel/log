package com.chobolevel.api.common.provider

import com.chobolevel.api.common.properties.ResendProperties
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ExternalApiException
import com.resend.Resend
import com.resend.core.exception.ResendException
import com.resend.services.emails.model.CreateEmailOptions
import org.springframework.stereotype.Component

@Component
class ResendEmailProvider(
    private val resendProperties: ResendProperties
) : EmailProvider {

    override fun sendEmail(to: String, subject: String, content: String) {
        val resend: Resend = Resend(resendProperties.apiKey)
        val params: CreateEmailOptions = CreateEmailOptions.builder()
            .from("[초로] <no-reply@chobolevel.com>")
            .to(to)
            .subject(subject)
            .html(content)
            .build()
        try {
            resend.emails().send(params)
        } catch (e: ResendException) {
            throw ExternalApiException(
                errorCode = ErrorCode.EMAIL_SEND_FAILED,
                message = "이메일 발송에 실패했습니다. (to=$to)",
                throwable = e
            )
        }
    }
}
