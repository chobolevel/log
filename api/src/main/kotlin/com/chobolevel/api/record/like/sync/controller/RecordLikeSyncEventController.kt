package com.chobolevel.api.record.like.sync.controller

import com.chobolevel.api.common.annotation.HasAuthorityAdmin
import com.chobolevel.api.common.annotation.QueryObject
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.dto.ResultResponse
import com.chobolevel.api.record.like.sync.dto.RecordLikeSyncEventResponse
import com.chobolevel.api.record.like.sync.dto.SearchRecordLikeSyncEventRequest
import com.chobolevel.api.record.like.sync.service.RecordLikeSyncEventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "RecordLikeSyncEvent (기록 좋아요 동기화 이벤트)", description = "기록 좋아요 동기화 이벤트 관리 API")
@RestController
@RequestMapping("/api/v1")
class RecordLikeSyncEventController(
    private val service: RecordLikeSyncEventService
) {

    @Operation(summary = "실패한 좋아요 동기화 이벤트 목록 조회 API")
    @HasAuthorityAdmin
    @GetMapping("/record-like-sync-events/failed")
    fun searchFailedEvents(
        @QueryObject request: SearchRecordLikeSyncEventRequest
    ): ResponseEntity<ResultResponse<PagingResponse<RecordLikeSyncEventResponse>>> {
        val result: PagingResponse<RecordLikeSyncEventResponse> = service.searchFailedEvents(request = request)
        return ResponseEntity.ok(ResultResponse(result))
    }

    @Operation(summary = "실패한 좋아요 동기화 이벤트 재발행 API")
    @HasAuthorityAdmin
    @PostMapping("/record-like-sync-events/{id}/retry")
    fun retry(@PathVariable id: Long): ResponseEntity<ResultResponse<Long>> {
        val result: Long = service.retry(eventId = id)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
