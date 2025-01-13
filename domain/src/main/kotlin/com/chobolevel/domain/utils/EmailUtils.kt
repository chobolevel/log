package com.chobolevel.domain.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailUtils(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}")
    private val from: String
) {

    fun sendEmail(email: String, subject: String, content: String) {
        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")
        helper.setFrom(from)
        helper.setTo(email)
        helper.setSubject(subject)
        helper.setText(content, false)
        mailSender.send(mimeMessage)
    }

    fun sendEmailByHtml(email: String, subject: String, content: String) {
        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "utf-8")
        helper.setFrom(from)
        helper.setTo(email)
        helper.setSubject(subject)
        helper.setText(content, true)
//        helper.addInline("image", ClassPathResource("/templates/test.jpg"))
        mailSender.send(mimeMessage)
    }
}
