package com.chobolevel.api.security

import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val authenticationProviders: List<AuthenticationProvider>,
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication {
        for (provider in authenticationProviders) {
            if (provider.supports(authentication.javaClass)) {
                return provider.authenticate(authentication) // 실제 인증 로직 실행
            }
        }
        throw ApiException(
            errorCode = ErrorCode.BAD_CREDENTIALS,
            status = HttpStatus.UNAUTHORIZED,
            message = "there is no suitable authentication provider"
        )
    }
}
