package com.chobolevel.api.service.auth

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.security.CustomAuthenticationManager
import com.chobolevel.api.security.TokenProvider
import com.chobolevel.api.service.auth.validator.LoginValidatable
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: CustomAuthenticationManager,
    private val redisTemplate: RedisTemplate<String, String>,
    private val loginValidators: List<LoginValidatable>,
    private val userFinder: UserFinder
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

    @Transactional(readOnly = true)
    fun reissue(refreshToken: String): JwtResponse {
        tokenProvider.validateToken(refreshToken)
        val authentication = tokenProvider.getAuthentication(refreshToken) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_TOKEN,
            status = HttpStatus.UNAUTHORIZED,
            message = "토큰이 만료되었습니다. 재로그인 해주세요."
        )
        val user = userFinder.findById(authentication.name.toLong())
        val cachedRefreshToken = opsForHash.get("refresh-token:v1", user.id!!.toString())
        if (cachedRefreshToken == null || cachedRefreshToken != refreshToken) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_TOKEN,
                status = HttpStatus.UNAUTHORIZED,
                message = "유효하지 않은 갱신 토큰입니다. 재로그인 해주세요."
            )
        }
        val result = tokenProvider.generateToken(authentication)
        return result
    }

    private fun setRefreshToken(userId: String, refreshToken: String) {
        opsForHash.put("refresh-token:v1", userId, refreshToken)
    }
}
