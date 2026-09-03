package com.chobolevel.api.user.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
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
import com.chobolevel.domain.common.exception.BadCredentialException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.InvalidParameterException
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.common.exception.UnAuthorizedException
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import io.hypersistence.tsid.TSID
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class UserAuthService(
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository,
    private val userConverter: UserConverter,
    private val passwordProvider: PasswordProvider,
    private val cacheProvider: CacheProvider,
    private val emailProvider: EmailProvider,
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
        val result: JwtResponse = tokenProvider.generateToken(authentication).also {
            setRefreshToken(authentication.name, it.refreshToken)
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
        val result: JwtResponse = tokenProvider.generateToken(authentication).also {
            setRefreshToken(authentication.name, it.refreshToken)
        }
        return result
    }

    @Transactional(readOnly = true)
    fun reissue(refreshToken: String): JwtResponse {
        tokenProvider.validateToken(refreshToken)
        val authentication: UsernamePasswordAuthenticationToken = tokenProvider.getAuthentication(refreshToken) as? UsernamePasswordAuthenticationToken ?: throw UnAuthorizedException(
            errorCode = ErrorCode.INVALID_TOKEN,
            message = "토큰이 만료되었습니다. 재로그인 해주세요."
        )
        val cachedUserId: String? = getUserIdByRefreshToken(refreshToken)
        if (cachedUserId == null || cachedUserId != authentication.name) {
            throw UnAuthorizedException(
                errorCode = ErrorCode.INVALID_TOKEN,
                message = "유효하지 않은 갱신 토큰입니다. 재로그인 해주세요."
            )
        }
        val result: JwtResponse = tokenProvider.generateToken(authentication)
        return result
    }

    fun sendEmailVerificationCode(request: SendEmailVerificationCodeRequest): Boolean {
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
        val cachedVerificationCode: String = cacheProvider.get(CacheKeyPrefix.EMAIL + request.email) ?: throw PolicyViolationException(
            errorCode = ErrorCode.EMAIL_VERIFICATION_CODE_NOT_SENT
        )
        if (request.verificationCode != cachedVerificationCode) {
            throw PolicyViolationException(
                errorCode = ErrorCode.EMAIL_VERIFICATION_CODE_NOT_MATCHED,
            )
        }
        cacheProvider.delete(CacheKeyPrefix.EMAIL + request.email)
        return request.email
    }

    fun logout(refreshToken: String) {
        removeRefreshToken(refreshToken)
    }

    private fun setRefreshToken(userId: String, refreshToken: String) {
        cacheProvider.put("refresh-token:v1:$refreshToken", userId)
    }

    private fun getUserIdByRefreshToken(refreshToken: String): String? {
        return cacheProvider.get("refresh-token:v1:$refreshToken")
    }

    private fun removeRefreshToken(refreshToken: String) {
        cacheProvider.delete("refresh-token:v1:$refreshToken")
    }
}
