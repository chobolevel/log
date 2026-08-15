package com.chobolevel.api.common.provider

interface EmailProvider {

    fun sendEmail(to: String, subject: String, content: String)
}
