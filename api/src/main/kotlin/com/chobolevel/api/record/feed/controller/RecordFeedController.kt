package com.chobolevel.api.record.feed.controller

import com.chobolevel.api.common.security.getUserId
import com.chobolevel.api.common.web.CursorPagingResponse
import com.chobolevel.api.common.web.GuestId
import com.chobolevel.api.common.web.QueryObject
import com.chobolevel.api.common.web.ResultResponse
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.feed.dto.SearchRecordFeedRequest
import com.chobolevel.api.record.feed.service.RecordFeedService
import com.chobolevel.api.record.feed.validator.RecordFeedParameterValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Record Feed (기록 피드)", description = "기록 피드 API")
@RestController
@RequestMapping("/api/v1")
class RecordFeedController(
    private val validator: RecordFeedParameterValidator,
    private val service: RecordFeedService
) {

    // 비로그인도 조회할 수 있다. 로그인하면 본인이 쓴 기록은 피드에서 빠지고, 로그인 여부와 무관하게 이미 조회한 기록도 빠진다.
    // 본 기록을 거르면 페이지가 size보다 짧거나 비어도 nextCursor가 있을 수 있다 — 클라이언트는 nextCursor가 있는 동안 계속 요청한다.
    @Operation(summary = "기록 피드 조회 API")
    @GetMapping("/records/feed")
    fun searchRecordFeeds(
        authentication: Authentication?,
        @GuestId guestId: String?,
        @QueryObject request: SearchRecordFeedRequest
    ): ResponseEntity<ResultResponse<CursorPagingResponse<RecordResponse>>> {
        validator.validate(request = request)
        val result: CursorPagingResponse<RecordResponse> = service.searchRecordFeeds(
            requesterId = authentication?.getUserId(),
            guestId = guestId,
            request = request
        )
        return ResponseEntity.ok(ResultResponse(result))
    }
}
