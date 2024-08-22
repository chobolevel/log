package com.chobolevel.api.controller.v1.user

import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.user.image.CreateUserImageRequestDto
import com.chobolevel.api.getUserId
import com.chobolevel.api.service.user.UserImageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "UserImage (회원 프로필 이미지)", description = "회원 프로필 이미지 관리 API")
@RestController
@RequestMapping("/api/v1")
class UserImageController(
    private val service: UserImageService
) {

    @Operation(summary = "회원 포로필 이미지 변경 API")
    @HasAuthorityUser
    @PostMapping("/users/images")
    fun createUserImage(
        principal: Principal,
        @Valid @RequestBody
        request: CreateUserImageRequestDto
    ): ResponseEntity<ResultResponse> {
        val result = service.createUserImage(principal.getUserId(), request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 프로필 이미지 삭제 API")
    @HasAuthorityUser
    @DeleteMapping("/users/images/{userImageId}")
    fun deleteUserImage(principal: Principal, @PathVariable userImageId: Long): ResponseEntity<ResultResponse> {
        val result = service.deleteUserImage(principal.getUserId(), userImageId)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
