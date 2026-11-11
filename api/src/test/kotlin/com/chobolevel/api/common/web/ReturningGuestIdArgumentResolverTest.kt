package com.chobolevel.api.common.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.core.MethodParameter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest

class ReturningGuestIdArgumentResolverTest : BehaviorSpec({

    class Handlers {
        fun annotated(@ReturningGuestId guestId: String?) = guestId
        fun plain(guestId: String?) = guestId
    }

    val resolver: ReturningGuestIdArgumentResolver = ReturningGuestIdArgumentResolver()
    val annotated: MethodParameter = MethodParameter(Handlers::class.java.getMethod("annotated", String::class.java), 0)
    val plain: MethodParameter = MethodParameter(Handlers::class.java.getMethod("plain", String::class.java), 0)

    fun resolve(request: MockHttpServletRequest): Any? = resolver.resolveArgument(annotated, null, ServletWebRequest(request), null)

    given("피드가 비회원 식별자를 받을 때") {
        `when`("@ReturningGuestId가 붙은 파라미터이면") {
            then("이 해석기가 처리한다") {
                resolver.supportsParameter(annotated) shouldBe true
                resolver.supportsParameter(plain) shouldBe false
            }
        }

        `when`("쿠키로 돌아온 식별자이면") {
            then("그 값을 돌려준다") {
                val request: MockHttpServletRequest = MockHttpServletRequest().apply { setAttribute(RequestAttributeKey.GUEST_ID, "guest-1") }

                resolve(request) shouldBe "guest-1"
            }
        }

        `when`("방금 발급한 식별자이면") {
            then("이력이 있을 수 없으므로 null을 돌려준다") {
                val request: MockHttpServletRequest = MockHttpServletRequest().apply {
                    setAttribute(RequestAttributeKey.GUEST_ID, "guest-new")
                    setAttribute(RequestAttributeKey.GUEST_ID_ISSUED, true)
                }

                resolve(request) shouldBe null
            }
        }

        `when`("식별자가 없으면") {
            then("null을 돌려준다") {
                resolve(MockHttpServletRequest()) shouldBe null
            }
        }
    }
})
