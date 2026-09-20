package com.chobolevel.api.user.follow.sync.controller

import com.chobolevel.api.common.annotation.HasAuthorityAdmin
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.user.follow.sync.dto.SearchUserFollowSyncEventRequest
import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventResponse
import com.chobolevel.api.user.follow.sync.service.UserFollowSyncEventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "UserFollowSyncEvent (팔로우 동기화 이벤트)", description = "팔로우 동기화 이벤트 관리 API")
@RestController
@RequestMapping("/api/v1")
class UserFollowSyncEventController(
    private val service: UserFollowSyncEventService
) {

    @Operation(summary = "실패한 팔로우 동기화 이벤트 목록 조회 API")
    @HasAuthorityAdmin
    @GetMapping("/user-follow-sync-events/failed")
    fun searchFailedEvents(
        @QueryObject request: SearchUserFollowSyncEventRequest
    ): ResponseEntity<ResultResponse<PagingResponse<UserFollowSyncEventResponse>>> {
        val result: PagingResponse<UserFollowSyncEventResponse> = service.searchFailedEvents(request = request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "실패한 팔로우 동기화 이벤트 재발행 API")
    @HasAuthorityAdmin
    @PostMapping("/user-follow-sync-events/{id}/retry")
    fun retry(@PathVariable id: Long): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.retry(eventId = id)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
