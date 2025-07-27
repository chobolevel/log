package com.chobolevel.api.controller.v1.auth

import com.chobolevel.api.dto.auth.CheckEmailVerificationCodeRequest
import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.auth.SendEmailVerificationCodeRequest
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.jwt.JwtResponse
import com.chobolevel.api.getCookie
import com.chobolevel.api.service.auth.AuthService
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth (인증)", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1")
class AuthController(
    @Value("\${server.reactive.session.cookie.access-token-key}")
    private val accessTokenKey: String,
    @Value("\${server.reactive.session.cookie.refresh-token-key}")
    private val refreshTokenKey: String,
    private val service: AuthService,
    private val serverProperties: ServerProperties
) {

    @Operation(summary = "회원 로그인 API")
    @PostMapping("/login")
    fun loginUser(
        res: HttpServletResponse,
        @Valid @RequestBody
        request: LoginRequestDto
    ): ResponseEntity<ResultResponse> {
        val result: JwtResponse = service.login(request)
        val accessTokenCookie: Cookie = generateCookie(
            key = accessTokenKey,
            value = result.accessToken
        )
        val refreshTokenCookie: Cookie = generateCookie(
            key = refreshTokenKey,
            value = result.refreshToken
        )
        res.addCookie(accessTokenCookie)
        res.addCookie(refreshTokenCookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    fun logout(req: HttpServletRequest, res: HttpServletResponse): ResponseEntity<ResultResponse> {
        val refreshToken: String? = req.getCookie(refreshTokenKey)
        if (refreshToken != null) {
            service.logout(refreshToken)
        }
        val expiredAccessTokenCookie: Cookie = generateCookie(
            key = "_cat",
            value = "",
            maxAge = 0
        )
        val expiredRefreshTokenCookie: Cookie = generateCookie(
            key = "_crt",
            value = "",
            maxAge = 0
        )
        res.addCookie(expiredAccessTokenCookie)
        res.addCookie(expiredRefreshTokenCookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "회원 토큰 재발급 API")
    @PostMapping("/reissue")
    fun reissueToken(
        req: HttpServletRequest,
        res: HttpServletResponse,
    ): ResponseEntity<ResultResponse> {
        val refreshToken: String = req.getCookie(refreshTokenKey) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_TOKEN,
            status = HttpStatus.UNAUTHORIZED,
            message = "토큰이 만료되었습니다. 재로그인 해주세요."
        )
        val result: JwtResponse = service.reissue(refreshToken)
        val newAccessTokenCookie: Cookie = generateCookie(
            key = accessTokenKey,
            value = result.accessToken
        )
        res.addCookie(newAccessTokenCookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "이메일 인증 코드 전송 API")
    @PostMapping("/send-verification-code/email")
    fun sendEmailVerificationCode(
        @Valid @RequestBody
        request: SendEmailVerificationCodeRequest
    ): ResponseEntity<ResultResponse> {
        service.asyncSendEmailVerificationCode(request)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "이메일 인증 코드 확인 API")
    @PostMapping("/check-verification-code/email")
    fun checkEmailVerificationCode(
        @Valid @RequestBody
        request: CheckEmailVerificationCodeRequest
    ): ResponseEntity<ResultResponse> {
        val result: String = service.checkEmailVerificationCode(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    private fun generateCookie(
        key: String,
        value: String,
        maxAge: Int = serverProperties.reactive.session.cookie.maxAge.toSeconds().toInt()
    ): Cookie {
        return Cookie(key, value).also {
            it.path = serverProperties.reactive.session.cookie.path
            it.maxAge = maxAge
            it.domain = serverProperties.reactive.session.cookie.domain
            it.secure = serverProperties.reactive.session.cookie.secure
            it.isHttpOnly = serverProperties.reactive.session.cookie.httpOnly
            it.setAttribute("SameSite", serverProperties.reactive.session.cookie.sameSite.attributeValue())
        }
    }
}
