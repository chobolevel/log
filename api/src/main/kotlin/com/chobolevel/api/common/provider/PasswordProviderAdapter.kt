package com.chobolevel.api.common.provider

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordProviderAdapter(
    private val encoder: BCryptPasswordEncoder,
) : PasswordProvider {

    override fun encode(plainText: String?): String {
        return encoder.encode(plainText)
    }

    override fun matches(plainText: String?, encodedText: String?): Boolean {
        return encoder.matches(plainText, encodedText)
    }
}
