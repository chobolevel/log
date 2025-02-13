package com.chobolevel.api.service.auth

import com.chobolevel.api.dto.auth.CheckEmailVerificationCodeRequest
import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.auth.SendEmailVerificationCodeRequest
import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.security.CustomAuthenticationManager
import com.chobolevel.api.security.TokenProvider
import com.chobolevel.api.service.auth.validator.AuthValidator
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.chobolevel.domain.utils.EmailUtils
import io.hypersistence.tsid.TSID
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: CustomAuthenticationManager,
    private val redisTemplate: RedisTemplate<String, String>,
    private val authValidator: AuthValidator,
    private val emailUtils: EmailUtils
) {

    private val opsForHash = redisTemplate.opsForHash<String, String>()

    @Transactional(readOnly = true)
    fun login(request: LoginRequestDto): JwtResponse {
        authValidator.validate(request)
        val authenticationToken = when (request.loginType) {
            UserLoginType.GENERAL -> UsernamePasswordAuthenticationToken(
                "${request.email}/${request.loginType}",
                request.password
            )

            else -> UsernamePasswordAuthenticationToken("${request.email}/${request.loginType}", request.socialId)
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
        val cachedUserId = getUserIdByRefreshToken(refreshToken)
        if (cachedUserId == null || cachedUserId != authentication.name) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_TOKEN,
                status = HttpStatus.UNAUTHORIZED,
                message = "유효하지 않은 갱신 토큰입니다. 재로그인 해주세요."
            )
        }
        val result = tokenProvider.generateToken(authentication)
        return result
    }

    @Async
    fun asyncSendEmailVerificationCode(request: SendEmailVerificationCodeRequest) {
        authValidator.validate(request)
        val authCode = TSID.fast().toString()
        opsForHash.put("email", request.email, authCode)
        emailUtils.sendEmail(
            email = request.email,
            subject = "[초로 - 이메일 인증 코드]",
            content = "초보 개발자의 로그 이메일 인증코드를 전송해드립니다.\n인증코드: [$authCode]"
        )
    }

    fun checkEmailVerificationCode(request: CheckEmailVerificationCodeRequest): String {
        authValidator.validate(request)
        val cachedVerificationCode = opsForHash.get("email", request.email) ?: throw ApiException(
            errorCode = ErrorCode.A001,
            message = "이메일 인증 코드 전송 후 시도해 주세요."
        )
        if (request.verificationCode != cachedVerificationCode) {
            throw ApiException(
                errorCode = ErrorCode.A002,
                message = "인증 코드가 일치하지 않습니다."
            )
        }
        opsForHash.delete("email", request.email)
        return request.email
    }

    fun logout(refreshToken: String) {
        removeRefreshToken(refreshToken)
    }

    private fun setRefreshToken(userId: String, refreshToken: String) {
        opsForHash.put("refresh-token:v1", refreshToken, userId)
    }

    private fun getUserIdByRefreshToken(refreshToken: String): String? {
        return opsForHash.get("refresh-token:v1", refreshToken)
    }

    private fun removeRefreshToken(refreshToken: String) {
        opsForHash.delete("refresh-token:v1", refreshToken)
    }
}
