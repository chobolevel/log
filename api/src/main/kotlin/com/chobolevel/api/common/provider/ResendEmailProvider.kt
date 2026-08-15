package com.chobolevel.api.common.provider

import com.chobolevel.api.common.properties.ResendProperties
import com.resend.Resend
import com.resend.core.exception.ResendException
import com.resend.services.emails.model.CreateEmailOptions
import org.springframework.stereotype.Component

@Component
class ResendEmailProvider(
    private val resendProperties: ResendProperties
) : EmailProvider {

    override fun sendEmail(to: String, subject: String, content: String) {
        val resend = Resend(resendProperties.apiKey)

        val params = CreateEmailOptions.builder()
            .from("[초로] <no-reply@chobolevel.com>")
            .to(to)
            .subject(subject)
            .html(content)
            .build()

        try {
            resend.emails().send(params)
        } catch (e: ResendException) {
            e.printStackTrace()
        }
    }
}
