package com.chobolevel.api.security

import com.chobolevel.api.getCookie
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class OnceJwtAuthorizationFilter(
    private val accessTokenKey: String,
    private val tokenProvider: TokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // session 사용하지 않기 때문에 context holder 인증 객체 있다면 넣어 주는 작업 수행
        // 토큰 없는 경우 다음 필터 수행
        val accessToken = request.getCookie(accessTokenKey)
        if (accessToken == null) {
            filterChain.doFilter(request, response)
            return
        }
        tokenProvider.getAuthentication(accessToken).also {
            SecurityContextHolder.getContext().authentication = it
        }
        filterChain.doFilter(request, response)
    }
}
