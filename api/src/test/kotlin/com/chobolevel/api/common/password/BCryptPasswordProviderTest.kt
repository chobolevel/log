package com.chobolevel.api.common.password

import com.chobolevel.domain.common.provider.PasswordProvider
import com.chobolevel.infrastructure.password.BCryptPasswordProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-2): 이동 전에 PasswordProvider 구현의 현재 계약을 고정한다.
// 외부 인프라가 필요 없는 단위 테스트다.
@DisplayName("BCryptPasswordProvider 계약 테스트")
class BCryptPasswordProviderTest {

    private val passwordProvider: PasswordProvider = BCryptPasswordProvider()

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

    @Test
    fun `인코딩한 값은 강도 10의 BCrypt 형식이다`() {
        // 강도(cost)가 바뀌면 새로 만드는 해시의 형식이 달라지므로 고정한다.
        assertThat(passwordProvider.encode("password1!")).startsWith("\$2a\$10\$")
    }

    @Test
    fun `이전에 저장된 해시와 계속 매칭된다`() {
        // 운영 DB에 이미 저장된 해시로 로그인할 수 있어야 하므로, 현재 구현이 만든 해시를 값으로 고정한다.
        val storedHash: String = "\$2a\$10\$a7CYOmRrik/ZoAYpzE3vW.1e51cvQhBf1dFENpZBEUcHO2Ah1aed6"

        assertThat(passwordProvider.matches("password1!", storedHash)).isTrue
        assertThat(passwordProvider.matches("password1?", storedHash)).isFalse
    }

    @Test
    fun `다른 강도로 만들어진 해시도 매칭된다`() {
        // BCrypt 해시에는 강도가 들어 있어서 강도를 바꾼 뒤에도 이전 해시로 로그인할 수 있다.
        val hashWithLowCost: String = "\$2a\$04\$DGRM5lKjDcMIWGv7GU1/V.RF1z6Py45j683b3jkaE8m8dhgqT5daS"

        assertThat(passwordProvider.matches("password1!", hashWithLowCost)).isTrue
    }
}
