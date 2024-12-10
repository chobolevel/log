package com.chobolevel.api.controller.v1.auth

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.auth.ReissueRequestDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.service.auth.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth (인증)", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1")
class AuthController(
    private val service: AuthService
) {

    @GetMapping("/oauth/authorize")
    fun authorize(
        res: HttpServletResponse,
        @RequestParam(required = false) clientId: String?,
        @RequestParam(required = false) clientSecret: String?,
        @RequestParam(required = false) redirectUrl: String?
    ) {
        // client 검증 로직
        if (clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty() || redirectUrl.isNullOrEmpty()) {
            return res.sendRedirect("/authorize")
        }
        return res.sendRedirect("/login")
    }

    @Operation(summary = "회원 로그인 API")
    @PostMapping("/login")
    fun loginUser(
        res: HttpServletResponse,
        @Valid @RequestBody
        request: LoginRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.login(request)
        val cookie = Cookie("_cau", result.accessToken).also {
            it.path = "/"
            it.domain = "chobolevel.site"
            it.maxAge = 60 * 60 * 24 * 365
            it.secure = true
            it.isHttpOnly = true
            it.setAttribute("SameSite", "None")
        }
        res.addCookie(cookie)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 토큰 재발급 API")
    @PostMapping("/reissue")
    fun reissueToken(
        @Valid @RequestBody
        request: ReissueRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.reissue(request)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
