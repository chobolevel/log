package com.chobolevel.api.user.controller

import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getCookie
import com.chobolevel.api.common.properties.JwtProperties
import com.chobolevel.api.user.dto.CheckEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.JwtResponse
import com.chobolevel.api.user.dto.LoginRequest
import com.chobolevel.api.user.dto.SendEmailVerificationCodeRequest
import com.chobolevel.api.user.dto.SocialLoginRequest
import com.chobolevel.api.user.service.UserAuthService
import com.chobolevel.api.user.validator.UserAuthParameterValidator
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.UnAuthorizedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth (인증)", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class UserAuthController(
    private val validator: UserAuthParameterValidator,
    private val service: UserAuthService,
    private val jwtProperties: JwtProperties
) {

    @Operation(summary = "일반 로그인 API")
    @PostMapping("/login")
    fun loginUser(
        res: HttpServletResponse,
        @Valid @RequestBody
        request: LoginRequest
    ): ResponseEntity<ResultResponse<Boolean>> {
        validator.validate(request = request)
        val result: JwtResponse = service.login(request)
        val accessTokenCookie: Cookie = generateCookie(
            key = jwtProperties.accessTokenKey,
            value = result.accessToken
        )
        val refreshTokenCookie: Cookie = generateCookie(
            key = jwtProperties.refreshTokenKey,
            value = result.refreshToken
        )
        res.addCookie(accessTokenCookie)
        res.addCookie(refreshTokenCookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "소셜 로그인 API")
    @PostMapping("/social-login")
    fun socialLoginUser(
        res: HttpServletResponse,
        @Valid @RequestBody
        request: SocialLoginRequest
    ): ResponseEntity<ResultResponse<Boolean>> {
        validator.validate(request = request)
        val result: JwtResponse = service.socialLogin(request)
        val accessTokenCookie: Cookie = generateCookie(
            key = jwtProperties.accessTokenKey,
            value = result.accessToken
        )
        val refreshTokenCookie: Cookie = generateCookie(
            key = jwtProperties.refreshTokenKey,
            value = result.refreshToken
        )
        res.addCookie(accessTokenCookie)
        res.addCookie(refreshTokenCookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    fun logout(req: HttpServletRequest, res: HttpServletResponse): ResponseEntity<ResultResponse<Boolean>> {
        val refreshToken: String? = req.getCookie(jwtProperties.refreshTokenKey)
        if (refreshToken != null) {
            service.logout(refreshToken)
        }
        val expiredAccessTokenCookie: Cookie = generateCookie(
            key = jwtProperties.accessTokenKey,
            value = "",
            maxAge = 0
        )
        val expiredRefreshTokenCookie: Cookie = generateCookie(
            key = jwtProperties.refreshTokenKey,
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
    ): ResponseEntity<ResultResponse<Boolean>> {
        val refreshToken: String = req.getCookie(jwtProperties.refreshTokenKey) ?: throw UnAuthorizedException(
            errorCode = ErrorCode.INVALID_TOKEN,
            message = "토큰이 만료되었습니다. 재로그인 해주세요."
        )
        val result: JwtResponse = service.reissue(refreshToken)
        val newAccessTokenCookie: Cookie = generateCookie(
            key = jwtProperties.accessTokenKey,
            value = result.accessToken
        )
        val newRefreshTokenCookie: Cookie = generateCookie(
            key = jwtProperties.refreshTokenKey,
            value = result.refreshToken
        )
        res.addCookie(newAccessTokenCookie)
        res.addCookie(newRefreshTokenCookie)
        return ResponseEntity.ok(ResultResponse(true))
    }

    @Operation(summary = "이메일 인증 코드 전송 API")
    @PostMapping("/email-verifications/send-code")
    fun sendEmailVerificationCode(
        @Valid @RequestBody
        request: SendEmailVerificationCodeRequest
    ): ResponseEntity<ResultResponse<Boolean>> {
        validator.validate(request = request)
        val result: Boolean = service.sendEmailVerificationCode(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "이메일 인증 코드 확인 API")
    @PostMapping("/email-verifications/verify-code")
    fun verifyEmailVerificationCode(
        @Valid @RequestBody
        request: CheckEmailVerificationCodeRequest
    ): ResponseEntity<ResultResponse<String>> {
        validator.validate(request = request)
        val result: String = service.checkEmailVerificationCode(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    private fun generateCookie(
        key: String,
        value: String,
        maxAge: Int = jwtProperties.cookie.maxAge
    ): Cookie {
        val cookieConfig: JwtProperties.Cookie = jwtProperties.cookie
        return Cookie(key, value).also {
            it.path = cookieConfig.path
            it.maxAge = maxAge
            it.domain = cookieConfig.domain
            it.secure = cookieConfig.secure
            it.isHttpOnly = cookieConfig.httpOnly
            it.setAttribute("SameSite", cookieConfig.sameSite)
        }
    }
}
