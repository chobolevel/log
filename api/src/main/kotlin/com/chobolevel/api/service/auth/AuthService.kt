package com.chobolevel.api.service.auth

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.security.CustomAuthenticationManager
import com.chobolevel.api.security.TokenProvider
import com.chobolevel.api.service.auth.validator.LoginValidatable
import com.chobolevel.domain.entity.user.UserLoginType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: CustomAuthenticationManager,
    private val redisTemplate: RedisTemplate<String, String>,
    private val loginValidators: List<LoginValidatable>
) {

    private val opsForHash = redisTemplate.opsForHash<String, String>()

    @Transactional(readOnly = true)
    fun login(request: LoginRequestDto): JwtResponse {
        loginValidators.forEach { it.validate(request) }
        val authenticationToken = when (request.loginType) {
            UserLoginType.GENERAL -> UsernamePasswordAuthenticationToken(
                "${request.email}/${request.loginType}",
                request.password
            )

            else -> UsernamePasswordAuthenticationToken("${request.socialId}/${request.loginType}", request.socialId)
        }
        val authentication = authenticationManager.authenticate(authenticationToken)
        val result = tokenProvider.generateToken(authentication).also {
            setRefreshToken(authentication.name, it.refreshToken)
        }
        return result
    }

    private fun setRefreshToken(userId: String, refreshToken: String) {
        opsForHash.put("refresh-token:v1", userId, refreshToken)
    }
}
