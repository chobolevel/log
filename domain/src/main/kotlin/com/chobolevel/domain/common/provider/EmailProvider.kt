package com.chobolevel.domain.common.provider

interface EmailProvider {

    fun sendEmail(to: String, subject: String, content: String)
}
