package com.chobolevel.api.common.provider

interface PasswordProvider {

    fun encode(plainText: String?): String

    fun matches(plainText: String?, encodedText: String?): Boolean
}
