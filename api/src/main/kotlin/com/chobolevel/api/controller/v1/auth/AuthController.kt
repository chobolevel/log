package com.chobolevel.api.controller.v1.auth

import com.chobolevel.api.dto.auth.LoginRequestDto
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.service.auth.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth (인증)", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1")
class AuthController(
    private val service: AuthService
) {

    @Operation(summary = "회원 로그인 API")
    @PostMapping("/login")
    fun loginUser(
        @Valid @RequestBody
        request: LoginRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.login(request)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
