package com.chobolevel.api.user.follow.controller

import com.chobolevel.api.common.annotation.HasAuthorityUser
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.common.extension.getUserId
import com.chobolevel.api.user.follow.dto.SearchUserFollowerRequest
import com.chobolevel.api.user.follow.dto.SearchUserFollowingRequest
import com.chobolevel.api.user.follow.dto.UserFollowResponse
import com.chobolevel.api.user.follow.service.UserFollowQueryService
import com.chobolevel.api.user.follow.service.UserFollowService
import com.chobolevel.api.user.follow.validator.UserFollowParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User Follow (회원 팔로우)", description = "회원 팔로우 API")
@RestController
@RequestMapping("/api/v1")
class UserFollowController(
    private val service: UserFollowService,
    private val queryService: UserFollowQueryService,
    private val validator: UserFollowParameterValidator,
) {

    @Operation(summary = "회원 팔로우 API")
    @HasAuthorityUser
    @PostMapping("/users/{userId}/follow")
    fun follow(
        authentication: Authentication,
        @PathVariable userId: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        validator.validateFollow(followerUserId = authentication.getUserId(), followingUserId = userId)
        val result: Boolean = service.follow(
            followerUserId = authentication.getUserId(),
            followingUserId = userId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 팔로우 취소 API")
    @HasAuthorityUser
    @PostMapping("/users/{userId}/unfollow")
    fun unfollow(
        authentication: Authentication,
        @PathVariable userId: Long
    ): ResponseEntity<ResultResponse<Boolean>> {
        val result: Boolean = service.unfollow(
            followerUserId = authentication.getUserId(),
            followingUserId = userId
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 팔로워 목록 조회 API")
    @GetMapping("/users/{userId}/followers")
    fun searchUserFollowers(
        @PathVariable userId: Long,
        @QueryObject request: SearchUserFollowerRequest
    ): ResponseEntity<ResultResponse<PagingResponse<UserFollowResponse>>> {
        val result: PagingResponse<UserFollowResponse> = queryService.searchUserFollowers(
            userId = userId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "회원 팔로잉 목록 조회 API")
    @GetMapping("/users/{userId}/followings")
    fun searchUserFollowings(
        @PathVariable userId: Long,
        @QueryObject request: SearchUserFollowingRequest
    ): ResponseEntity<ResultResponse<PagingResponse<UserFollowResponse>>> {
        val result: PagingResponse<UserFollowResponse> = queryService.searchUserFollowings(
            userId = userId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
