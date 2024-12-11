package com.chobolevel.api.controller.v1.auth

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.service.auth.AuthService
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.http.HttpHeaders
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
        val result = service.login(request)
        // set access token
        res.setHeader(HttpHeaders.AUTHORIZATION, "${result.tokenType} ${result.accessToken}")
        // set refresh token
        val cookie = Cookie("_crt", result.refreshToken).also {
            it.path = serverProperties.reactive.session.cookie.path
            it.maxAge = serverProperties.reactive.session.cookie.maxAge.toSeconds().toInt()
            it.domain = serverProperties.reactive.session.cookie.domain
            it.secure = serverProperties.reactive.session.cookie.secure
            it.isHttpOnly = serverProperties.reactive.session.cookie.httpOnly
            it.setAttribute("SameSite", serverProperties.reactive.session.cookie.sameSite.attributeValue())
        }
        res.addCookie(cookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    fun logout(res: HttpServletResponse): ResponseEntity<ResultResponse> {
        val cookie = Cookie("_crt", "").also {
            it.maxAge = 0
            it.path = serverProperties.reactive.session.cookie.path
            it.isHttpOnly = serverProperties.reactive.session.cookie.httpOnly
            it.secure = serverProperties.reactive.session.cookie.secure
        }
        res.addCookie(cookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "회원 토큰 재발급 API")
    @PostMapping("/reissue")
    fun reissueToken(
        req: HttpServletRequest,
        res: HttpServletResponse,
    ): ResponseEntity<ResultResponse> {
        if (req.cookies.isNullOrEmpty() || req.cookies.find { it.name == "_crt" } == null) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_TOKEN,
                status = HttpStatus.UNAUTHORIZED,
                message = "토큰이 만료되었습니다. 재로그인 해주세요."
            )
        }
        val refreshToken = req.cookies.first { it.name.equals("_crt") }.value
        val result = service.reissue(refreshToken)
        res.setHeader(HttpHeaders.AUTHORIZATION, "${result.tokenType} ${result.accessToken}")
        return ResponseEntity.ok(ResultResponse(true))
    }
}
