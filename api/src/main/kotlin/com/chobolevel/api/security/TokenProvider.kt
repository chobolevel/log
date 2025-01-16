package com.chobolevel.api.security

import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.properties.JwtProperties
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Header
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class TokenProvider(
    private val jwtProperties: JwtProperties,
    private val userDetailService: UserDetailsService
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun generateToken(authentication: Authentication): JwtResponse {
        val now = Date()
        val accessTokenExpiredAt = Date(now.time + TimeUnit.HOURS.toMillis(1))
        val refreshTokenExpiredAt = Date(now.time + TimeUnit.DAYS.toMillis(30))
        val accessToken = issueAccessToken(
            issuedAt = now,
            expiration = accessTokenExpiredAt,
            authentication = authentication
        )
        val refreshToken = issueRefreshToken(
            issuedAt = now,
            expiration = refreshTokenExpiredAt,
            authentication = authentication
        )
        return JwtResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun issueAccessToken(issuedAt: Date, expiration: Date, authentication: Authentication): String {
        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer(jwtProperties.issuer)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .setSubject(authentication.name)
            .claim("authorities", authentication.authorities)
            .signWith(SignatureAlgorithm.HS256, jwtProperties.secret)
            .compact()
    }

    private fun issueRefreshToken(issuedAt: Date, expiration: Date, authentication: Authentication): String {
        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer(jwtProperties.issuer)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .setSubject(authentication.name)
            .claim("authorities", authentication.authorities)
            .signWith(SignatureAlgorithm.HS256, jwtProperties.secret)
            .compact()
    }

    fun getAuthentication(token: String): Authentication? {
        return try {
            validateToken(token)
            val claims = Jwts.parser()
                .setSigningKey(jwtProperties.secret)
                .parseClaimsJws(token)
                .body
            val userDetails = userDetailService.loadUserByUsername(claims.subject)
            UsernamePasswordAuthenticationToken(userDetails, token, userDetails.authorities)
        } catch (e: Exception) {
            logger.warn("Token is invalid", e)
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .setSigningKey(jwtProperties.secret)
                .parseClaimsJws(token)
            true
        } catch (e: ExpiredJwtException) {
            throw ApiException(
                errorCode = ErrorCode.EXPIRED_TOKEN,
                status = HttpStatus.UNAUTHORIZED,
                message = "토큰이 만료되었습니다."
            )
        } catch (e: JwtException) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_TOKEN,
                status = HttpStatus.UNAUTHORIZED,
                message = "유효하지 않은 토큰입니다."
            )
        }
    }
}
