package com.chobolevel.api.common.security

import com.chobolevel.domain.common.exception.BadCredentialException
import com.chobolevel.domain.common.exception.ErrorCode
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val authenticationProviders: List<AuthenticationProvider>,
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication {
        return authenticationProviders
            .find { it.supports(authentication::class.java) }
            ?.authenticate(authentication)
            ?: throw BadCredentialException(
                errorCode = ErrorCode.BAD_CREDENTIALS,
                message = "there is no suitable authentication provider"
            )
    }
}
