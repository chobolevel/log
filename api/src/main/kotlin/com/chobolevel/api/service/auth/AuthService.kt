package com.chobolevel.api.service.auth

import com.chobolevel.api.dto.auth.CheckEmailVerificationCodeRequest
import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.auth.SendEmailVerificationCodeRequest
import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.security.CustomAuthenticationManager
import com.chobolevel.api.security.TokenProvider
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.chobolevel.domain.utils.EmailUtils
import io.hypersistence.tsid.TSID
import org.springframework.data.redis.core.HashOperations
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: CustomAuthenticationManager,
    private val opsForHash: HashOperations<String, String, String>,
    private val emailUtils: EmailUtils
) {

    @Transactional(readOnly = true)
    fun login(request: LoginRequestDto): JwtResponse {
        val authenticationToken: UsernamePasswordAuthenticationToken = when (request.loginType) {
            UserLoginType.GENERAL -> UsernamePasswordAuthenticationToken(
                "${request.email}/${request.loginType}",
                request.password
            )

            else -> UsernamePasswordAuthenticationToken("${request.email}/${request.loginType}", request.socialId)
        }
        val authentication: Authentication = authenticationManager.authenticate(authenticationToken)
        val result: JwtResponse = tokenProvider.generateToken(authentication).also {
            setRefreshToken(authentication.name, it.refreshToken)
        }
        return result
    }

    @Transactional(readOnly = true)
    fun reissue(refreshToken: String): JwtResponse {
        tokenProvider.validateToken(refreshToken)
        val authentication: Authentication = tokenProvider.getAuthentication(refreshToken) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_TOKEN,
            status = HttpStatus.UNAUTHORIZED,
            message = "토큰이 만료되었습니다. 재로그인 해주세요."
        )
        val cachedUserId: String? = getUserIdByRefreshToken(refreshToken)
        if (cachedUserId == null || cachedUserId != authentication.name) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_TOKEN,
                status = HttpStatus.UNAUTHORIZED,
                message = "유효하지 않은 갱신 토큰입니다. 재로그인 해주세요."
            )
        }
        val result: JwtResponse = tokenProvider.generateToken(authentication)
        return result
    }

    @Async
    fun asyncSendEmailVerificationCode(request: SendEmailVerificationCodeRequest) {
        val authCode: String = TSID.fast().toString()
        opsForHash.put("email", request.email, authCode)
        emailUtils.sendEmail(
            email = request.email,
            subject = "[초로 - 이메일 인증 코드]",
            content = "초보 개발자의 로그 이메일 인증코드를 전송해드립니다.\n인증코드: [$authCode]"
        )
    }

    fun checkEmailVerificationCode(request: CheckEmailVerificationCodeRequest): String {
        val cachedVerificationCode: String = opsForHash.get("email", request.email) ?: throw ApiException(
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
