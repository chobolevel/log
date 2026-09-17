package com.chobolevel.api.common.security

import com.chobolevel.api.common.constant.RequestAttributeKey
import com.chobolevel.api.common.extension.getCookie
import com.chobolevel.api.common.properties.GuestProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

// 방문자(회원/비회원 공통) 식별을 위한 게스트 ID 발급 필터.
// 로그인 여부와 무관하게 항상 guestId를 보장한다 — 조회 이력/추천 피드 등에서
// 비회원 방문자를 안정적으로 구분하기 위해 사용된다.
class GuestIdAssignFilter(
    private val guestProperties: GuestProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val guestId: String = request.getCookie(guestProperties.guestIdKey) ?: issueGuestId(response)
        request.setAttribute(RequestAttributeKey.GUEST_ID, guestId)
        filterChain.doFilter(request, response)
    }

    private fun issueGuestId(response: HttpServletResponse): String {
        val guestId: String = UUID.randomUUID().toString()
        response.addCookie(generateCookie(guestId))
        return guestId
    }

    private fun generateCookie(guestId: String): Cookie {
        val cookieConfig: GuestProperties.Cookie = guestProperties.cookie
        return Cookie(guestProperties.guestIdKey, guestId).also {
            it.path = cookieConfig.path
            it.maxAge = cookieConfig.maxAge
            it.domain = cookieConfig.domain
            it.secure = cookieConfig.secure
            it.isHttpOnly = cookieConfig.httpOnly
            it.setAttribute("SameSite", cookieConfig.sameSite)
        }
    }
}
