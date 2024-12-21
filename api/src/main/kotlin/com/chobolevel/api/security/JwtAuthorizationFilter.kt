package com.chobolevel.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException

class JwtAuthorizationFilter(
    private val authManager: CustomAuthenticationManager,
    private val tokenProvider: TokenProvider
) : BasicAuthenticationFilter(authManager) {

    @Throws(IOException::class, ServletException::class, AccessDeniedException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        // session 사용하지 않기 때문에 context holder 인증 객체 있다면 넣어 주는 작업 수행
        // 토큰 없는 경우 다음 필터 수행
        if (request.cookies.isNullOrEmpty() || request.cookies.find { it.name == "_cat" } == null) {
            chain.doFilter(request, response)
            return
        }
        val accessToken = request.cookies.find { it.name == "_cat" }!!.value
        tokenProvider.getAuthentication(accessToken).also {
            SecurityContextHolder.getContext().authentication = it
        }
        chain.doFilter(request, response)
    }
}
