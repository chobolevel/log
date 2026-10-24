package com.chobolevel.api.common.password

import com.chobolevel.domain.common.provider.PasswordProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-2): 이동 전에 PasswordProvider 구현의 현재 계약을 고정한다.
// 외부 인프라가 필요 없는 단위 테스트다.
@DisplayName("BCryptPasswordProvider 계약 테스트")
class BCryptPasswordProviderTest {

    private val passwordProvider: PasswordProvider = BCryptPasswordProvider(BCryptPasswordEncoder())

    @Test
    fun `인코딩한 값은 평문과 다르고 BCrypt 형식이다`() {
        // when
        val encoded: String = passwordProvider.encode("password1!")

        // then
        assertThat(encoded).isNotEqualTo("password1!")
        assertThat(encoded).startsWith("\$2")
    }

    @Test
    fun `같은 평문도 인코딩할 때마다 다른 값이 나온다`() {
        // when
        val first: String = passwordProvider.encode("password1!")
        val second: String = passwordProvider.encode("password1!")

        // then
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `평문과 인코딩 값이 맞으면 true, 틀리면 false다`() {
        // given
        val encoded: String = passwordProvider.encode("password1!")

        // when & then
        assertThat(passwordProvider.matches("password1!", encoded)).isTrue
        assertThat(passwordProvider.matches("password1?", encoded)).isFalse
        assertThat(passwordProvider.matches("PASSWORD1!", encoded)).isFalse
        assertThat(passwordProvider.matches("", encoded)).isFalse
    }

    @Test
    fun `서로 다르게 인코딩된 두 값이 모두 같은 평문과 맞는다`() {
        // given
        val first: String = passwordProvider.encode("password1!")
        val second: String = passwordProvider.encode("password1!")

        // when & then
        assertThat(passwordProvider.matches("password1!", first)).isTrue
        assertThat(passwordProvider.matches("password1!", second)).isTrue
    }

    @Test
    fun `인코딩 값이 null이거나 비어 있으면 false다`() {
        assertThat(passwordProvider.matches("password1!", null)).isFalse
        assertThat(passwordProvider.matches("password1!", "")).isFalse
    }

    @Test
    fun `평문이 null이면 예외가 발생한다`() {
        // given
        val encoded: String = passwordProvider.encode("password1!")

        // when & then: 현재 구현(BCryptPasswordEncoder)의 동작을 그대로 고정한다.
        assertThrows<IllegalArgumentException> { passwordProvider.encode(null) }
        assertThrows<IllegalArgumentException> { passwordProvider.matches(null, encoded) }
    }
}
