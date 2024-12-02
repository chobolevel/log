package com.chobolevel.api.controller.v1.user

import com.chobolevel.api.annotation.HasAuthorityAdmin
import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.user.ChangeUserPasswordRequest
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.api.getUserId
import com.chobolevel.api.service.user.UserService
import com.chobolevel.api.service.user.query.UserQueryCreator
import com.chobolevel.domain.entity.user.UserLoginType
import com.chobolevel.domain.entity.user.UserOrderType
import com.chobolevel.domain.entity.user.UserRoleType
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "User (회원)", description = "회원 관리 API")
@RestController
@RequestMapping("/api/v1")
class UserController(
    private val service: UserService,
    private val queryCreator: UserQueryCreator
) {

    @Operation(summary = "회원 가입 API")
    @PostMapping("/users")
    fun createUser(
        @Valid @RequestBody
        request: CreateUserRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.createUser(request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 목록 조회 API")
    @GetMapping("/users")
    fun searchUsers(
        principal: Principal?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) loginType: UserLoginType?,
        @RequestParam(required = false) nickname: String?,
        @RequestParam(required = false) phone: String?,
        @RequestParam(required = false) role: UserRoleType?,
        @RequestParam(required = false) resigned: Boolean?,
        @RequestParam(required = false) skipCount: Long?,
        @RequestParam(required = false) limitCount: Long?,
        @RequestParam(required = false) orderTypes: List<UserOrderType>?
    ): ResponseEntity<ResultResponse> {
        val queryFilter = queryCreator.createQueryFilter(
            email = email,
            loginType = loginType,
            nickname = nickname,
            phone = phone,
            role = role,
            resigned = resigned,
            excludeUserIds = principal?.let { listOfNotNull(it.getUserId()) } ?: emptyList()
        )
        val pagination = queryCreator.createPaginationFilter(
            skipCount = skipCount,
            limitCount = limitCount
        )
        val result = service.searchUserList(queryFilter, pagination, orderTypes)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 단건 조회 API")
    @HasAuthorityAdmin
    @GetMapping("/users/{id}")
    fun fetchUser(@PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result = service.fetchUser(id)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 본인 정보 조회 API")
    @HasAuthorityUser
    @GetMapping("/users/me")
    fun myUser(principal: Principal): ResponseEntity<ResultResponse> {
        val result = service.fetchUser(principal.getUserId())
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 정보 수정 API")
    @HasAuthorityUser
    @PutMapping("/users/{id}")
    fun updateUser(
        principal: Principal,
        @PathVariable id: Long,
        @RequestBody @Valid
        request: UpdateUserRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.updateUser(principal.getUserId(), request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 비밀번호 변경 API")
    @HasAuthorityUser
    @PutMapping("/users/change-password")
    fun changePassword(
        principal: Principal,
        @RequestBody request: ChangeUserPasswordRequest
    ): ResponseEntity<ResultResponse> {
        val result = service.changePassword(principal.getUserId(), request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 탈퇴 API")
    @HasAuthorityUser
    @DeleteMapping("/users/{id}")
    fun resignUser(principal: Principal, @PathVariable id: Long): ResponseEntity<ResultResponse> {
        val result = service.resignUser(principal.getUserId())
        return ResponseEntity.ok(ResultResponse(result))
    }
}
