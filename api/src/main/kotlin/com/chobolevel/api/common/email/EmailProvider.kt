package com.chobolevel.api.common.email
interface EmailProvider {

    fun sendEmail(to: String, subject: String, content: String)
}
