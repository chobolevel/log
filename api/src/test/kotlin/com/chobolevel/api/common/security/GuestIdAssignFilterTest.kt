package com.chobolevel.api.common.security

import com.chobolevel.api.common.web.RequestAttributeKey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.Cookie
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class GuestIdAssignFilterTest : BehaviorSpec({

    val guestIdKey: String = "_test_gid"
    val filter: GuestIdAssignFilter = GuestIdAssignFilter(
        GuestProperties(
            guestIdKey = guestIdKey,
            cookie = GuestProperties.Cookie(path = "/", maxAge = 100, domain = "localhost", secure = false, httpOnly = true, sameSite = "none")
        )
    )

    given("요청이 들어올 때") {
        `when`("비회원 식별자 쿠키가 있으면") {
            then("그 값을 쓰고 새로 발급했다고 표시하지 않는다") {
                val request: MockHttpServletRequest = MockHttpServletRequest().apply { setCookies(Cookie(guestIdKey, "existing-guest")) }
                val response: MockHttpServletResponse = MockHttpServletResponse()

                filter.doFilter(request, response, MockFilterChain())

                request.getAttribute(RequestAttributeKey.GUEST_ID) shouldBe "existing-guest"
                request.getAttribute(RequestAttributeKey.GUEST_ID_ISSUED) shouldBe null
                response.getCookie(guestIdKey) shouldBe null
            }
        }

        `when`("쿠키가 없으면") {
            then("새 식별자를 발급해 쿠키로 내려주고 새로 발급했다고 표시한다") {
                val request: MockHttpServletRequest = MockHttpServletRequest()
                val response: MockHttpServletResponse = MockHttpServletResponse()

                filter.doFilter(request, response, MockFilterChain())

                val issued: String = request.getAttribute(RequestAttributeKey.GUEST_ID) as String
                issued.isNotBlank() shouldBe true
                request.getAttribute(RequestAttributeKey.GUEST_ID_ISSUED) shouldBe true
                response.getCookie(guestIdKey)?.value shouldBe issued
            }
        }

        `when`("쿠키 없는 요청이 연달아 오면") {
            then("요청마다 서로 다른 식별자를 발급한다") {
                val first: MockHttpServletRequest = MockHttpServletRequest()
                val second: MockHttpServletRequest = MockHttpServletRequest()

                filter.doFilter(first, MockHttpServletResponse(), MockFilterChain())
                filter.doFilter(second, MockHttpServletResponse(), MockFilterChain())

                (first.getAttribute(RequestAttributeKey.GUEST_ID) == second.getAttribute(RequestAttributeKey.GUEST_ID)) shouldBe false
            }
        }
    }
})
