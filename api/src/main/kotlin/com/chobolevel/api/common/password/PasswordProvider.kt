package com.chobolevel.api.common.password
interface PasswordProvider {

    fun encode(plainText: String?): String

    fun matches(plainText: String?, encodedText: String?): Boolean
}
