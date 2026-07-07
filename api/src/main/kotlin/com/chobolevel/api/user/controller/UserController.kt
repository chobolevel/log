package com.chobolevel.api.user.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.user.dto.ChangeUserPasswordRequest
import com.chobolevel.api.user.dto.CreateUserRequest
import com.chobolevel.api.user.dto.SearchUserRequest
import com.chobolevel.api.user.dto.UpdateUserRequest
import com.chobolevel.api.user.dto.UserPageRequest
import com.chobolevel.api.user.dto.UserResponseDto
import com.chobolevel.api.user.service.UserService
import com.chobolevel.api.user.validator.UserParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "User (회원)", description = "회원 관리 API")
@RestController
@RequestMapping("/api/v1")
class UserController(
    private val validator: UserParameterValidator,
    private val service: UserService
) {

    @Operation(summary = "회원 가입 API")
    @PostMapping("/users")
    fun createUser(
        @Valid @RequestBody
        request: CreateUserRequest
    ): ResponseEntity<ResultResponse> {
        validator.validate(request = request)
        val result: Long = service.createUser(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 목록 조회 API")
    @GetMapping("/users")
    fun searchUsers(
        filter: SearchUserRequest,
        pageRequest: UserPageRequest
    ): ResponseEntity<ResultResponse> {
        val result: PagingResponse = service.searchUsers(
            filter = filter,
            pageRequest = pageRequest,
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 단건 조회 API")
    @GetMapping("/users/{id}")
    fun fetchUser(@PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result: UserResponseDto = service.fetchUser(id)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 본인 정보 조회 API")
    @HasAuthorityUser
    @GetMapping("/users/me")
    fun myUser(principal: Principal): ResponseEntity<ResultResponse> {
        val result: UserResponseDto = service.fetchUser(principal.getUserId())
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 정보 수정 API")
    @HasAuthorityUser
    @PutMapping("/users/me")
    fun updateUser(
        principal: Principal,
        @RequestBody @Valid
        request: UpdateUserRequest
    ): ResponseEntity<ResultResponse> {
        validator.validate(request = request)
        val result: Long = service.updateUser(principal.getUserId(), request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 비밀번호 변경 API")
    @HasAuthorityUser
    @PutMapping("/users/change-password")
    fun changePassword(
        principal: Principal,
        @RequestBody request: ChangeUserPasswordRequest
    ): ResponseEntity<ResultResponse> {
        validator.validate(request = request)
        val result: Long = service.changePassword(principal.getUserId(), request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 탈퇴 API")
    @HasAuthorityUser
    @DeleteMapping("/users/me")
    fun resignUser(principal: Principal): ResponseEntity<ResultResponse> {
        val result: Boolean = service.resignUser(principal.getUserId())
        return ResponseEntity.ok(ResultResponse(result))
    }
}
