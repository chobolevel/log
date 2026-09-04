package com.chobolevel.api.user.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.common.provider.EmailProvider
import com.chobolevel.api.common.provider.PasswordProvider
import com.chobolevel.api.common.security.TokenProvider
import com.chobolevel.api.user.converter.UserConverter
import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.JwtResponse
import com.chobolevel.api.user.dto.LoginRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.SocialLoginRequest
import com.chobolevel.api.user.validator.UserBusinessValidator
import com.chobolevel.domain.common.exception.BadCredentialException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.common.exception.UnAuthorizedException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.hypersistence.tsid.TSID
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Date
import java.util.concurrent.TimeUnit

@Service
class UserAuthService(
    private val userRepository: UserRepository,
    private val userConverter: UserConverter,
    private val tokenProvider: TokenProvider,
    private val passwordProvider: PasswordProvider,
    private val cacheProvider: CacheProvider,
    private val emailProvider: EmailProvider,
    private val userBusinessValidator: UserBusinessValidator
) {

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): JwtResponse {
        val user: User? = userRepository.findByEmailOrNull(request.email)
        if (user == null || !passwordProvider.matches(request.password, user.password)) {
            throw BadCredentialException(
                errorCode = ErrorCode.BAD_CREDENTIALS,
                message = "아이디 또는 비밀번호가 일치하지 않습니다."
            )
        }
        val authorities: List<GrantedAuthority> = AuthorityUtils.createAuthorityList(user.role.name)
        val authentication: UsernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(user.id, user.password, authorities)
        val result: JwtResponse = tokenProvider.generateTokenPair(authentication).also {
            setRefreshToken(
                userId = user.id!!,
                refreshToken = it.refreshToken,
                refreshTokenExpiredAt = it.refreshTokenExpiredAt,
            )
        }
        return result
    }

    @Transactional
    fun socialLogin(request: SocialLoginRequest): JwtResponse {
        val existingUser: User? = userRepository.findByEmailOrNull(request.email)
        if (existingUser != null && existingUser.loginType != request.loginType) {
            throw InvalidParameterException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                message = "소셜 로그인에 실패했습니다."
            )
        }
        val user: User = existingUser ?: userRepository.save(userConverter.convert(request))
        if (user.socialId != request.socialId) {
            user.socialId = request.socialId
        }
        val authorities: List<GrantedAuthority> = AuthorityUtils.createAuthorityList(user.role.name)
        val authentication: UsernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(user.id, user.password, authorities)
        val result: JwtResponse = tokenProvider.generateTokenPair(authentication).also {
            setRefreshToken(
                userId = user.id!!,
                refreshToken = it.refreshToken,
                refreshTokenExpiredAt = it.refreshTokenExpiredAt,
            )
        }
        return result
    }

    @Transactional(readOnly = true)
    fun reissue(refreshToken: String): JwtResponse {
        tokenProvider.validateToken(refreshToken)

        val authentication: Authentication = tokenProvider.getAuthentication(refreshToken) ?: throw UnAuthorizedException(
            errorCode = ErrorCode.INVALID_TOKEN,
            message = "토큰이 만료되었습니다. 재로그인 해주세요."
        )

        val userId: Long = authentication.getUserId()
        val cachedRefreshToken: String? = getRefreshTokenByUserId(userId = userId)
        if (cachedRefreshToken == null || cachedRefreshToken != refreshToken) {
            throw UnAuthorizedException(
                errorCode = ErrorCode.INVALID_TOKEN,
                message = "유효하지 않은 갱신 토큰입니다. 재로그인 해주세요."
            )
        }

        val result: JwtResponse = tokenProvider.generateTokenPair(authentication).also {
            setRefreshToken(
                userId = userId,
                refreshToken = it.refreshToken,
                refreshTokenExpiredAt = it.refreshTokenExpiredAt,
            )
        }
        return result
    }

    fun sendEmailVerificationCode(request: SendEmailVerificationCodeRequest): Boolean {
        userBusinessValidator.validate(request = request)
        val authCode: String = TSID.fast().toString()
        cacheProvider.put(CacheKeyPrefix.EMAIL + request.email, authCode, 5, TimeUnit.MINUTES)
        val emailBody: String = javaClass.getResourceAsStream("/templates/email/verification-code.html")
            ?.bufferedReader()
            ?.readText()
            ?.replace("{{code}}", authCode)
            ?: authCode
        emailProvider.sendEmail(
            to = request.email,
            subject = "[초로] 이메일 인증 코드",
            content = emailBody
        )
        return true
    }

    fun checkEmailVerificationCode(request: CheckEmailVerificationCodeRequest): String {
        val cachedVerificationCode: String? = cacheProvider.get(CacheKeyPrefix.EMAIL + request.email)
        if (request.verificationCode != cachedVerificationCode) {
            throw InvalidParameterException(
                errorCode = ErrorCode.EMAIL_VERIFICATION_CODE_NOT_MATCHED,
            )
        }
        cacheProvider.delete("${CacheKeyPrefix.EMAIL}${request.email}")
        return request.email
    }

    fun logout(refreshToken: String) {
        val authentication: Authentication = tokenProvider.getAuthentication(refreshToken) ?: return
        clearRefreshTokenByUserId(userId = authentication.getUserId())
    }

    private fun setRefreshToken(userId: Long, refreshToken: String, refreshTokenExpiredAt: Date) {
        val ttlMillis: Long = refreshTokenExpiredAt.time - System.currentTimeMillis()
        cacheProvider.put(
            "${CacheKeyPrefix.REFRESH_TOKEN}$userId",
            refreshToken,
            ttlMillis,
            TimeUnit.MILLISECONDS
        )
    }

    private fun getRefreshTokenByUserId(userId: Long): String? {
        return cacheProvider.get("${CacheKeyPrefix.REFRESH_TOKEN}$userId")
    }

    private fun clearRefreshTokenByUserId(userId: Long) {
        cacheProvider.delete("${CacheKeyPrefix.REFRESH_TOKEN}$userId")
    }
}
