package com.chobolevel.api.service.auth

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.security.CustomAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: CustomAuthenticationManager
) {

    @Transactional(readOnly = true)
    fun login(request: LoginRequestDto): JwtResponse {
        val authenticationToken =
            UsernamePasswordAuthenticationToken("${request.email}/${request.loginType}", request.password)
        val authentication = authenticationManager.authenticate(authenticationToken)
        return JwtResponse(
            accessToken = "",
            refreshToken = ""
        )
    }
}
