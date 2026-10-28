package com.chobolevel.infrastructure.password

import com.chobolevel.domain.common.provider.PasswordProvider
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

// 인코더를 빈으로 노출하지 않고 이 클래스가 직접 소유한다. 해시 방식(BCrypt, 강도 10)이 이 구현의 내부 사정이기 때문이다.
@Component
class BCryptPasswordProvider : PasswordProvider {

    private val encoder: BCryptPasswordEncoder = BCryptPasswordEncoder(STRENGTH)

    override fun encode(plainText: String?): String {
        return encoder.encode(plainText)
    }

    override fun matches(plainText: String?, encodedText: String?): Boolean {
        return encoder.matches(plainText, encodedText)
    }

    private companion object {
        const val STRENGTH: Int = 10
    }
}
